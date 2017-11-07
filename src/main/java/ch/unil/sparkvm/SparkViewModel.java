package ch.unil.sparkvm;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.Spark;
import spark.TemplateEngine;
import spark.template.jtwig.JtwigTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author gushakov
 */
public class SparkViewModel {

    private static final Logger logger = LoggerFactory.getLogger(SparkViewModel.class);

    public static void run(String scanPath) {

        if (scanPath == null || scanPath.isEmpty()) {
            throw new IllegalStateException("Scan path cannot be null or empty");
        }

        // JSON serialization
        final Gson gson = new Gson();

        // JTwig rendering engine
        final TemplateEngine templateEngine = new JtwigTemplateEngine("/META-INF/resources/templates");

        // find any com.google.inject.Module implementations under the scan path
        final Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(scanPath))
                .setScanners(new SubTypesScanner())
        );
        final Set<Class<? extends Module>> modules = reflections.getSubTypesOf(Module.class);

        // create Guice injector
        final Injector injector;
        injector = Guice.createInjector(modules.stream()
                .filter(c -> !c.equals(AbstractModule.class))
                .map((Function<Class<? extends Module>, Module>) aClass -> {
                    try {
                        logger.info("Processing Guice module: {}", aClass.getSimpleName());
                        return aClass.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new IllegalStateException(e);
                    }
                }).collect(Collectors.toList()));

        // from https://killertilapia.blogspot.ch/2014/09/sparkjava-and-webjars.html
        // serve static resources (prepare for a webjar)
        Spark.staticFileLocation("/META-INF/resources");

        // home
        Spark.get("/", (request, response) ->
                new ModelAndView(new HashMap<>(), "main.twig.html"), templateEngine);

        // navigation
        Spark.get("/:template", (request, response) ->
                new ModelAndView(new HashMap<>(), request.params(":template") + ".twig.html"), templateEngine);

        // dynamic MVVM
        Spark.post("/dynamic", (request, response) -> {
            response.type("application/json");

            // get class of view-model and operation from request headers
            final String vmClass = request.headers("vmClass");
            final String operation = request.headers("operation");

            logger.debug("Processing dynamic request, uri: {}, vmClass: {}, operation: {}", request.uri(), vmClass, operation);

            // type of the view-model
            final Class<?> vmType = Class.forName(vmClass);
            // view-model instance
            final Object vm;

            switch (operation) {
                case "init":
                    // just instantiate the view-model using no-arguments constructor
                    vm = injector.getInstance(vmType);
                    break;
                default:
                    // parse POST body
                    final List<NameValuePair> body = URLEncodedUtils.parse(request.body(), StandardCharsets.UTF_8);

                    // get the serialized model
                    final String model = body.stream()
                            .filter(pair -> pair.getName().equals("model"))
                            .findFirst().orElseThrow(IllegalStateException::new)
                            .getValue();

                    // deserialze view-model
                    vm = gson.fromJson(model, vmType);

                    // wire any services
                    injector.injectMembers(vm);

                    // find and execute a method corresponding to the requested operation, uses reflection
                    switch (body.size()) {
                        case 1:
                            // no-args
                            vmType.getMethod(operation).invoke(vm);
                            break;
                        case 2:
                            // one argument
                            vmType.getMethod(operation, String.class).invoke(vm, body.get(1).getValue());
                            break;
                        case 3:
                            // two arguments
                            vmType
                                    .getMethod(operation, String.class, String.class)
                                    .invoke(vm, body.get(1).getValue(), body.get(2).getValue());
                            break;
                        default:
                            // three arguments
                            vmType
                                    .getMethod(operation, String.class, String.class, String.class)
                                    .invoke(vm, body.get(1).getValue(),
                                            body.get(2).getValue(),
                                            body.get(3).getValue());
                            break;

                    }
            }

            // serialize view-model
            return gson.toJson(vm);

        });
    }

}
