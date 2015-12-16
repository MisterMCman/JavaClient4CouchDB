package de.hshannover;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.cli.*;

import de.hshannover.couchapp.CouchApp;

public class RedditCouch {

    public static void main(String[] args) throws IOException {
    		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    		System.out.print("Enter Command:");
    		String line = null;
    		while(!"exit".equals(line)) {
    			line = br.readLine();
    			if("exit".equals(line)) {
    				return;
    			} else {
    				CommandLineParser parser = new DefaultParser();

            Options options = new Options();
            addOptions(options);

            try {
                CommandLine cmd = parser.parse( options, line.split(" ") );

                processCLI(cmd, options);
            } catch( ParseException exp ) {
                System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
                help(options);
            }

    			}
    		}
    }

    private static void processCLI(CommandLine line, Options options) {
        if (line.hasOption("h")) {
            help(options);
            return;
        }
        
        CouchApp capp = new CouchApp();
        Boolean dbUsed = false;
        Boolean process = false;
        if(line.hasOption("fetch")) {
            capp.fetch(line.getOptionValue("fetch"));
            dbUsed= true;
            process = true;
        }
        if(line.hasOption("use")) {
        	capp.use(line.getOptionValue("use"));
        	dbUsed = true;
        }
        if(dbUsed) {
            if(line.hasOption("process")) {
                capp.process();
                process = false;
            }
            if(process) {
            	System.out.println("after fetching a db you have to call process");
            } else {
                if(line.hasOption("friends")) {
                    capp.friends(line.getOptionValue("friends"));
                }
                if(line.hasOption("degreeCentrality")) {
                    capp.degreeCentrality(line.getOptionValue("degreeCentrality"));
                }
                if(line.hasOption("degreeCentralityMinMax")) {
                    capp.degreeCentralityMinMax();
                }
                if(line.hasOption("bridges")) {
                    //TODO
                }
            }
        } else {
        	System.out.println("you have to select a database with fetch or use");
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
    }
}
