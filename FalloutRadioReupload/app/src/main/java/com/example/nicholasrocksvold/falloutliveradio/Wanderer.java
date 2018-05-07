package com.example.nicholasrocksvold.falloutliveradio;

import android.net.Uri;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Calendar;

/**
 * Created by Grant Sipes 5/6/2018
 */

public class Wanderer {

    private final double SCALE = 24.0*128.0/16.0; //24hours * scale
    private final long HOUR_TO_MILLI = 3600000;
    private final double VAULT_TO_MEGATON = 9.0/16.0;
    private final double MEGATON_TO_GNR = 9.0/4.0;
    private final double GNR_TO_MUSEUM = 17.0/16.0;
    private final double GNR_TO_MONUMENT = 13.0/16.0;
    private final double MUSEUM_TO_MONUMENT = 1.0/2.0;

    private int karma;
    private boolean gender; //true=male false=female
    private boolean isNotDone;
    private String currentPlace;
    Radio radio;
    private Random random = new Random();

    private ArrayList<Quest> availableQuests = new ArrayList<>();
    private ArrayList<Quest> toBeDoneQuests = new ArrayList<>();
    private Quest currentQuest;

    private String uriPath = "android.resource://com.example.nicholasrocksvold.falloutliveradio/raw/";


    public Wanderer(Radio radio){
        this.karma = 0;
        this.gender = true;
        this.isNotDone = true;
        this.radio = radio;
        this.currentPlace = "Vault";

        toBeDoneQuests.add(new Quest(1,
                new String[]{"GNR","Museum","Monument","GNR"},
                10800000, -10, 10,
                null, new Uri[]{Uri.parse(uriPath+"escape1"), Uri.parse(uriPath+"intro1")}, null, null, -1));

        availableQuests.add(new Quest(0, new String[]{"Megaton"}, 10800000, -10, 10, null, null, null, null, 1));
    }

    public void start()
    {
        Calendar waitTime = Calendar.getInstance();
        waitTime.setTimeInMillis(-1);

        while(isNotDone) {
            availableQuests.trimToSize();
            int chosen = random.nextInt(availableQuests.size());
            System.out.println("Size of available: "+availableQuests.size());
            currentQuest = availableQuests.get(chosen);
            availableQuests.remove(chosen);

            currentQuest.printQuest();

            if(currentQuest.removeFromRadio(true) != null)
                for(int i=0; i<currentQuest.removeFromRadio(true).length; i++)
                    radio.removeFromNews(currentQuest.removeFromRadio(true)[i]);
            if(currentQuest.addToRadio(true) != null)
                radio.addToNews(currentQuest.addToRadio(true));

            for(int i = 0; i < currentQuest.getDestination().length; i++)
            {
                switch (currentPlace)
                {
                    case "Vault":
                        if(currentQuest.getDestination()[i].equals("Megaton")) {
                            waitTime.setTimeInMillis((long)((HOUR_TO_MILLI / SCALE) * VAULT_TO_MEGATON));
                        }
                        break;
                    case "Megaton":
                        if(currentQuest.getDestination()[i].equals("GNR"))
                            waitTime.setTimeInMillis((long)(HOUR_TO_MILLI*MEGATON_TO_GNR/SCALE));
                        else if(currentQuest.getDestination()[i].equals("Megaton"))
                            waitTime.setTimeInMillis((long)(HOUR_TO_MILLI*VAULT_TO_MEGATON/SCALE));
                        else
                            System.out.println("ERROR IN NEXT DESTINATION!");
                        break;
                    case "GNR":
                        if(currentQuest.getDestination()[i].equals("Megaton"))
                            waitTime.setTimeInMillis((long)(HOUR_TO_MILLI*MEGATON_TO_GNR/SCALE));
                        else if(currentQuest.getDestination()[i].equals("Museum"))
                            waitTime.setTimeInMillis((long)(HOUR_TO_MILLI*GNR_TO_MUSEUM/SCALE));
                        else if(currentQuest.getDestination()[i].equals("Monument"))
                            waitTime.setTimeInMillis((long)(HOUR_TO_MILLI*GNR_TO_MONUMENT/SCALE));
                        else
                            System.out.println("ERROR IN NEXT DESTINATION!");
                        break;
                    case "Museum":
                        if(currentQuest.getDestination()[i].equals("GNR"))
                            waitTime.setTimeInMillis((long)(HOUR_TO_MILLI*GNR_TO_MUSEUM/SCALE));
                        else if(currentQuest.getDestination()[i].equals("Monument"))
                            waitTime.setTimeInMillis((long)(HOUR_TO_MILLI*MUSEUM_TO_MONUMENT/SCALE));
                        else
                            System.out.println("ERROR IN NEXT DESTINATION!");
                        break;
                    case "Monument":
                        if(currentQuest.getDestination()[i].equals("GNR"))
                            waitTime.setTimeInMillis((long)(HOUR_TO_MILLI*GNR_TO_MONUMENT/SCALE));
                        else if(currentQuest.getDestination()[i].equals("Museum"))
                            waitTime.setTimeInMillis((long)(HOUR_TO_MILLI*GNR_TO_MONUMENT/SCALE));
                        else
                            System.out.println("ERROR IN NEXT DESTINATION!");
                        break;
                    default:
                        System.out.println("ERROR IN NEXT DESTINATION!");
                }

                Calendar startTime = Calendar.getInstance();
                Calendar timeElapsed = Calendar.getInstance();// = 0;
                timeElapsed.setTimeInMillis(0);
                while(timeElapsed.getTimeInMillis() <= waitTime.getTimeInMillis())
                {
                    timeElapsed.setTimeInMillis(Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis());

                    try{ Thread.sleep(10000);}catch(InterruptedException e){}
                    System.out.println("Wanderer is traveling from: "+currentPlace);
                    System.out.println("Wanderer is traveling to  : "+currentQuest.getDestination()[i]);
                    System.out.println("Wanderer's progress is    :"+timeElapsed.getTimeInMillis()+" out of "+waitTime.getTimeInMillis());
                }
            }

            if(currentQuest.getNextQuest() != -1)
                availableQuests.add(chosen, findQuestById(currentQuest.getNextQuest()));

            if(availableQuests.size() == 0)
                isNotDone = false;

            if(currentQuest.removeFromRadio(false) != null)
                for(int i=0; i<currentQuest.removeFromRadio(false).length; i++)
                    radio.removeFromNews(currentQuest.removeFromRadio(false)[i]);

            if(currentQuest.addToRadio(false) != null)
                radio.addToNews(currentQuest.addToRadio(false));
        }

        System.out.println("-=Finished Questing=-");
    }

    private Quest findQuestById(int id)
    {
        for(Quest quest : toBeDoneQuests)
            if(quest.getIdentifier() == id)
                return quest;

        return null;
    }

    public int getKarma(){
        return this.karma;
    }

    public void setKarma(int karma){
        this.karma = karma;
    }

    public boolean getGender(){
        return this.gender;
    }

    public void setGender(boolean gender){
        this.gender = gender;
    }

    public boolean isNotDone(){
        return this.isNotDone;
    }

    public void setNotDone(boolean notDone){
        this.isNotDone = notDone;
    }
}

/*
try{ Thread.sleep(10000);}catch(InterruptedException e){}
            System.out.println("Wanderer 10 seconds has passed");
 */

