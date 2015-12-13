package de.hshannover;

import org.apache.commons.cli.*;

public class RedditCouch {

    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();
        addOptions(options);

        try {
            CommandLine line = parser.parse( options, args );

            processCLI(line, options);
        } catch( ParseException exp ) {
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
            help(options);
        }
    }

    private static void processCLI(CommandLine line, Options options) {
        if (line.hasOption("h"))
            help(options);
        if(line.hasOption("fetch")) {
            //TODO
        }
        if(line.hasOption("use")) {
            //TODO
        }
        if(line.hasOption("friends")) {
            //TODO
        }
        if(line.hasOption("degreeCentrality")) {
            //TODO
        }
        if(line.hasOption("process")) {
            //TODO
        }
        if(line.hasOption("degreeCentralityMinMax")) {
            //TODO
        }
        if(line.hasOption("bridges")) {
            //TODO
        }
    }

    private static void addOptions(Options options) {
        addDetailedOption(options, "fetch", "urlToSubReddit", true, "fetches Subreddit to CouchDb");
        addDetailedOption(options, "use", "nameOfSubReddit", true, "uses Subreddit from CouchDb");
        addDetailedOption(options, "friends", "keyToUser", true, "shows all friends of friends from a given user");
        addDetailedOption(options, "degreeCentrality", "keyToUser", true, "shows degreecentrality from a given user");
        options.addOption("process", false, "processes fetched data (saves friends of nodes in nodes)");
        options.addOption("degreeCentralityMinMax", false, "calculates min and max degreecentrality of subreddit");
        options.addOption("bridges", false, "calculates all bridges of the subreddit");
        options.addOption("h", "help", false, "show help.");
    }

    private static void addDetailedOption(Options options, String opt, String argName, Boolean hasArg, String desc){
        options.addOption(Option.builder(opt)
                .argName(argName)
                .hasArg(hasArg)
                .desc(desc)
                .build());
    }

    private static void help(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("RedditCouch", options);
        System.exit(0);
    }
}
