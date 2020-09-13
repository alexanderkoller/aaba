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
        Javalin app = Javalin.create().start(7000);

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
        try {
            String arrangedAbc = arrange(ctx.formParam("input_abc"));
            ctx.render("index.jte", Map.of("abc", arrangedAbc));
//            ctx.result(arrangedFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            ctx.result("IOException!");
        } catch (AbcParser.AbcParsingException e) {
            e.printStackTrace();
            ctx.result("Parsing exception!");
        } catch (NoValidArrangementException e) {
            e.printStackTrace();
            ctx.result("Arranging exception!");
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


//            File file = File.createTempFile("aaba", ".abc");
//            file.deleteOnExit();
//            FileWriter fw = new FileWriter(file);
//            abcw.write(bestArrangedScore, fw);
//            fw.flush();
//            fw.close();
//
//            System.err.println("Written to " + file.getAbsolutePath());
            return w.toString();
        }
    }
}
