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
import spark.Session;
import spark.Spark;
import spark.TemplateEngine;
import spark.template.jtwig.JtwigTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
        Spark.get("/", (request, response) -> {

                    // invalidate session if refresh is requested on the home page
                    if (request.queryParams("refresh") != null) {
                        request.session().invalidate();
                    }
                    return new ModelAndView(new HashMap<>(), "main.twig.html");
                }, templateEngine);

        // navigation
        Spark.get("/:template", (request, response) ->
                new ModelAndView(new HashMap<>(), request.params(":template") + ".twig.html"), templateEngine);

        // dynamic MVVM
        Spark.post("/dynamic", (request, response) -> {
            response.type("application/json");

            // get the session
            final Session session = request.session();

            // get class of view-model and operation from request headers
            final String vmClass = request.headers("vmClass");
            final String operation = request.headers("operation");

            logger.debug("[{}] Processing dynamic request, uri: {}, vmClass: {}, operation: {}", session.id(), request.uri(), vmClass, operation);

            // type of the view-model
            final Class<?> vmType = Class.forName(vmClass);

            // view-model instance
            final Object vm;

            final String jsonIn = session.attribute(vmClass);
            if (jsonIn != null) {
                // deserialze view-model from the json stored in the session
                vm = gson.fromJson(jsonIn, vmType);
                // wire dependencies
                injector.injectMembers(vm);
            } else {
                // or request a new instance from the container
                vm = injector.getInstance(vmType);
            }

            // skip "init" operation, the initialization logic is processed already
            if (!operation.equals("init")) {

                // parse POST body
                final List<NameValuePair> body = URLEncodedUtils.parse(request.body(), StandardCharsets.UTF_8);

                // find and execute a method corresponding to the requested operation, uses reflection
                String arg1;
                String arg2;
                String arg3;
                switch (body.size()) {
                    case 0:
                        // no-args
                        logger.debug("[{}] Calling {}", session.id(), operation);
                        vmType.getMethod(operation).invoke(vm);
                        break;
                    case 1:
                        // one argument
                        arg1 = body.get(0).getValue();
                        logger.debug("[{}] Calling {} with arguments {}", session.id(), operation, Arrays.toString(new Object[]{arg1}));
                        vmType.getMethod(operation, String.class).invoke(vm, arg1);
                        break;
                    case 2:
                        // two arguments
                        arg1 = body.get(0).getValue();
                        arg2 = body.get(1).getValue();
                        logger.debug("[{}] Calling {} with arguments {}", session.id(), operation, Arrays.toString(new Object[]{arg1, arg2}));
                        vmType
                                .getMethod(operation, String.class, String.class)
                                .invoke(vm, arg1, arg2);
                        break;
                    default:
                        // three arguments
                        arg1 = body.get(0).getValue();
                        arg2 = body.get(1).getValue();
                        arg3 = body.get(2).getValue();
                        logger.debug("[{}] Calling {} with arguments {}", session.id(), operation, Arrays.toString(new Object[]{arg1, arg2, arg3}));
                        vmType
                                .getMethod(operation, String.class, String.class, String.class)
                                .invoke(vm, arg1, arg2, arg3);
                        break;

                }
            }


            // serialize view-model to JSON
            final String jsonOut = gson.toJson(vm);

            // store view-model in the session
            logger.debug("[{}] Storing JSON serialized view-model {} for view-model class {}", session.id(), jsonOut, vmClass);
            session.attribute(vmClass, jsonOut);

            return jsonOut;

        });
    }

}
