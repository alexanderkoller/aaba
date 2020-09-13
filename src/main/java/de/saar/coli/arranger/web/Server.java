package de.saar.coli.arranger.web;

import au.com.codeka.carrot.CarrotEngine;
import au.com.codeka.carrot.CarrotException;
import au.com.codeka.carrot.Configuration;
import au.com.codeka.carrot.bindings.MapBindings;
import au.com.codeka.carrot.resource.MemoryResourceLocator;
import au.com.codeka.carrot.resource.ResourceLocator;
import de.saar.coli.arranger.Arrange;
import de.saar.coli.arranger.Arrangement;
import de.saar.coli.arranger.Config;
import de.saar.coli.arranger.Score;
import de.saar.coli.arranger.abc.AbcParser;
import de.saar.coli.arranger.abc.AbcWriter;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.io.*;
import java.util.Map;

import static de.saar.coli.arranger.Arrange.loadConfig;
import static de.saar.coli.arranger.Util.slurp;

/**
 * A web server for running AABA. It serves a single HTML page on which the user can
 * enter the original melody and chords; these will be arranged and the result shown
 * on the website.<p>
 *
 * The server runs on port 7000 by default. If you set the environment  variable PORT
 * to some other number, the server will listen on that port instead.
 */
public class Server {
    private final Config config;
    private final CarrotEngine engine;
    private final AbcWriter abcw;

    public static void main(String[] args) throws FileNotFoundException {
        Server x = new Server();
        x.run();
    }

    public Server() throws FileNotFoundException {
        config = loadConfig(null);
        config.setAbcDialect(Config.ABC_DIALECT.ABC2SVG);
        abcw = new AbcWriter(config);

        engine = new CarrotEngine(new Configuration.Builder()
                .setResourceLocator(makeResourceLocator())
                .build());
    }

    public void run() throws FileNotFoundException {
        int port = 7000;

        if( System.getenv("PORT") != null ) {
            port = Integer.parseInt(System.getenv("PORT"));
        }

        Javalin app = Javalin.create(config -> {
            config.addStaticFiles("/static");
        }).start(port);

        app.get("/", ctx -> {
            getIndex(ctx);
        });

        app.post("/", ctx -> {
            postIndex(ctx);
        });
    }

    public void getIndex(Context ctx) {
        ctx.html(renderIndex(Map.of()));
    }

    public void postIndex(Context ctx) {
        try(AabaForm form =  AabaForm.parse(ctx)) {
            try {
                if( "".equals(form.input_abc())) {
                    ctx.html(renderIndex(Map.of("form", form, "error", "Please enter a song in ABC notation.")));
                } else {
                    Arrangement arrangement = arrange(form.input_abc());
                    ctx.html(renderIndex(Map.of("abc", abcw.asString(arrangement.getArrangement()), "form", form, "original_abc", form.input_abc(), "meta", arrangement)));
                }
            } catch (IOException e) {
                // This should never happen, we are not doing any I/O.
            } catch (AbcParser.AbcParsingException e) {
                e.printStackTrace();
                ctx.html(renderIndex(Map.of("form", form, "error", "ABC syntax error: " + e.getMessage())));
            } catch (NoValidArrangementException e) {
                e.printStackTrace();
                ctx.html(renderIndex(Map.of("form", form, "original_abc", form.input_abc(), "error", "Could not find a valid arrangement.")));
            }
        } catch (FormValidationException e) {
            e.printStackTrace();
            ctx.html(renderIndex(Map.of("error", "Please enter a song in ABC format.")));
        }
    }

    private String renderIndex(Map<String,Object> parameters) {
        try {
            return engine.process("index.html", new MapBindings(parameters));
        } catch (CarrotException e) {
            e.printStackTrace();
            return "(rendering exception: " + e + ")";
        }
    }

    private Arrangement arrange(String abcString) throws IOException, AbcParser.AbcParsingException, NoValidArrangementException {
        Score score = new AbcParser().read(new StringReader(abcString));

        Arrange arranger = new Arrange(config);
        Arrangement arrangement = arranger.arrange(score);

        if( arrangement == null ) {
            throw new NoValidArrangementException();
        } else {
            return arrangement;
        }
    }

    private ResourceLocator.Builder makeResourceLocator() {
        MemoryResourceLocator.Builder ret = new MemoryResourceLocator.Builder();
        ret.add("index.html", slurp("/carrot/index.html"));
        return ret;
    }
}
