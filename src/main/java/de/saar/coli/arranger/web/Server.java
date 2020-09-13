package de.saar.coli.arranger.web;

import de.saar.coli.arranger.Arrange;
import de.saar.coli.arranger.Config;
import de.saar.coli.arranger.Score;
import de.saar.coli.arranger.abc.AbcParser;
import de.saar.coli.arranger.abc.AbcWriter;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.io.*;
import java.util.Map;

import static de.saar.coli.arranger.Arrange.loadConfig;

public class Server {
    private final Config config;

    public static void main(String[] args) throws FileNotFoundException {
        Server x = new Server();
        x.run();
    }

    public Server() throws FileNotFoundException {
        config = loadConfig(null);
        config.setAbcDialect(Config.ABC_DIALECT.ABC2SVG);
    }

    public void run() throws FileNotFoundException {
        int port = 7000;

        if( System.getenv("PORT") != null ) {
            port = Integer.parseInt(System.getenv("PORT"));
        }

        Javalin app = Javalin.create().start(port);

        app.get("/", ctx -> {
            getIndex(ctx);
        });

        app.post("/", ctx -> {
            postIndex(ctx);
        });
    }

    public void getIndex(Context ctx) {
        ctx.render("index.jte");
    }

    public void postIndex(Context ctx) {
        try(AabaForm form =  AabaForm.parse(ctx)) {
            try {
                if( "".equals(form.input_abc())) {
                    ctx.render("index.jte", Map.of("form", form, "error", "Please enter a song in ABC notation."));
                } else {
                    String arrangedAbc = arrange(form.input_abc());
                    ctx.render("index.jte", Map.of("abc", arrangedAbc, "form", form));
                }
            } catch (IOException e) {
                // This should never happen, we are not doing any I/O.
            } catch (AbcParser.AbcParsingException e) {
                e.printStackTrace();
                ctx.render("index.jte", Map.of("form", form, "error", "ABC syntax error: " + e.getMessage()));
            } catch (NoValidArrangementException e) {
                e.printStackTrace();
                ctx.render("index.jte", Map.of("form", form, "error", "Could not find a valid arrangement."));
            }
        } catch (FormValidationException e) {
            e.printStackTrace();
            ctx.render("index.jte", Map.of("error", "Please enter a song in ABC format."));
        }
    }

    private String arrange(String abcString) throws IOException, AbcParser.AbcParsingException, NoValidArrangementException {
        Score score = new AbcParser().read(new StringReader(abcString));
        Arrange arranger = new Arrange(config);
        Score bestArrangedScore = arranger.arrange(score);

        if( bestArrangedScore == null ) {
            throw new NoValidArrangementException();
        } else {
            AbcWriter abcw = new AbcWriter(config);
            StringWriter w = new StringWriter();
            abcw.write(bestArrangedScore, w);
            return w.toString();
        }
    }
}
