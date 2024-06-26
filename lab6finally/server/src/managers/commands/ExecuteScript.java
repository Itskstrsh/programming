package managers.commands;

import data.*;
import managers.CommandManager;
import managers.PersonManager;
import network.Request;
import network.Response;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeSet;


public class ExecuteScript extends Command implements Serializable {
    @Serial
    private static final long serialVersionUID=4L;
    private Request request;

    HashMap<String, Command> command;
    public ExecuteScript(HashMap<String, Command> commands){
        super("execute_script", true);
        this.command=commands;

    }
    @Override
    public Response execute(Request request){
        executeScript((String)request.getArgs()[0]);
        return new Response("Script exxecuted");
    }

    public void executeScript(String filePath) {
        File file = new File(filePath);
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    String[] tokens = line.split("\\s+");
                    Command command = CommandManager.getCommands().get(tokens[0]);
                    if (command != null) {
                        // Check if the command is to execute another script
                        if (tokens[0].equals("execute_script") && tokens.length > 1) {
                            // Prevent infinite recursion by tracking already executed scripts
                            String nestedScriptPath = tokens[1];
                            executeScript(nestedScriptPath);
                        } else if (tokens[0].equals("add")) {
                            // Execute the add command by providing data for creating a new person inside the script
                            addPerson(tokens);
                        } else {
                            // Execute other commands normally
                            command.execute(request);
                        }
                    } else {
                        System.out.println("No such command: " + tokens[0]);
                    }
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error reading file: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    private void addPerson(String[] tokens) {
        TreeSet<Person> set = PersonManager.getCollection();
        // Check if there are enough arguments to create a person
        if (tokens.length >= 9) {
            // Extract data for creating a new person from the script arguments
            String name = tokens[1];
            Integer coordinateX = Integer.parseInt(tokens[2]);
            Integer coordinateY = Integer.parseInt(tokens[3]);
            Coordinates coordinates = new Coordinates(coordinateX, coordinateY);
            Double height = Double.parseDouble(tokens[4]);
            EyeColor eyeColor = tokens[5].equals("null") ? null : EyeColor.valueOf(tokens[5]);
            HairColor hairColor = tokens[6].equals("null") ? null : HairColor.valueOf(tokens[6]);
            Country nationality = tokens[7].equals("null") ? null : Country.valueOf(tokens[7]);
            String locationName = tokens[8];
            int locationX = Integer.parseInt(tokens[9]);
            double locationY = Double.parseDouble(tokens[10]);
            Float locationZ = tokens[11].equals("null") ? null : Float.parseFloat(tokens[11]);
            // Create and add the person to the collection
            Location location = new Location(locationName, locationX, locationY, locationZ);
            Person person = new Person(name, coordinates, height, eyeColor, hairColor, nationality, location);
            PersonManager.addPers(person);
            System.out.printf("Person %s added to collection. Size of collection: %d%n", person.getName(), set.size());
        } else {
            System.out.println("Not enough arguments to create a person.");
        }
    }



}
