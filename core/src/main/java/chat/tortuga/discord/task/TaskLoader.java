package chat.tortuga.discord.task;

import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class TaskLoader {

    private static TaskLoader instance;
    private static final Reflections REFLECTIONS = new Reflections(
            "chat.tortuga.discord",
            Scanners.TypesAnnotated,
            Scanners.MethodsAnnotated);
    private ScheduledExecutorService executor;

    private TaskLoader() {}
    public synchronized static TaskLoader getInstance() {
        if (instance == null) {
            instance = new TaskLoader();
        }
        return instance;
    }

    public void start() {
        log.debug("Loading tasks...");
        final Set<Class<?>> types = getTypeTasks();
        final Set<Method> methods = getMethodTasks();
        int size = types.size() + methods.size();
        executor = Executors.newScheduledThreadPool(size);

        types.forEach(t -> scheduleTask(t.getAnnotation(Task.class), asRunnable(t), t.getName()));
        methods.forEach(m -> scheduleTask(m.getAnnotation(Task.class), asRunnable(m), m.getName()));
        log.info("Loaded {} task(s)", size);
    }

    public void shutdown() {
        if (executor == null || executor.isShutdown()) {
            log.info("Scheduler has not been started or has already been shut down");
            return;
        }
        log.info("Stopping scheduler - cancelled tasks: {}", executor.shutdownNow());
        executor.close();
    }

    protected void scheduleTask(Task annotation, Runnable runnable, String name) {
        if (annotation == null) return;

        final long delay = Long.parseLong(annotation.delay());
        final long period = Long.parseLong(annotation.period());
        final TimeUnit unit = TimeUnit.valueOf(annotation.unit());

        log.debug("Scheduling task {} to run with a delay of {} {} and period of {} {}", name, delay, unit, period, unit);
        executor.scheduleAtFixedRate(runnable, delay, period, unit);
    }

    protected Set<Class<?>> getTypeTasks() {
        return REFLECTIONS.getTypesAnnotatedWith(Task.class)
                .stream()
                .filter(c -> isTaskEnabled(c.getName()))
                .collect(Collectors.toSet());
    }

    protected Set<Method> getMethodTasks() {
        return REFLECTIONS.getMethodsAnnotatedWith(Task.class)
                .stream()
                .filter(m -> isTaskEnabled(m.getName()) && Modifier.isStatic(m.getModifiers()))
                .collect(Collectors.toSet());
    }

    protected Boolean isTaskEnabled(String name) {
        // TODO read config
        return true;
    }

    protected Runnable asRunnable(Class<?> type) {
        try {
            return (Runnable) type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error("Error instantiating class {} as Runnable", type.getName(), e);
            return null;
        }
    }

    protected Runnable asRunnable(Method method) {
        return () -> {
            try {
                method.invoke(null);
            } catch (Exception e) {
                log.error("Error invoking method {}#{}", method.getClass().getName(), method.getName(), e);
            }
        };
    }

}
