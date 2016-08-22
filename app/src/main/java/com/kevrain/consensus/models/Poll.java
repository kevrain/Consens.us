package com.kevrain.consensus.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseRelation;

/**
 * Created by kfarst on 8/19/16.
 */
@ParseClassName("Poll")
public class Poll extends ParseObject {

    public Poll() {}

    public ParseRelation<Vote> getVotesRelation () {
        return getRelation("vote");
    }

    public void addVote(Vote user) {
        getVotesRelation().add(user);
        saveInBackground();
    }

    public void removeVote(Vote user) {
        getVotesRelation().remove(user);
        saveInBackground();
    }

    public void setGroup(Group group) {
        put("group", group);
    }

    public Group getGroup() {
        return (Group) getParseObject("group");
    }

    public void addLocation(Location location) {
        getLocationRelation().add(location);
        saveInBackground();
    }

    public ParseRelation<Location> getLocationRelation () {
        return getRelation("location");
    }

    public void removeLocation(Location location) {
        getLocationRelation().remove(location);
        saveInBackground();
    }

    public void setPollName(String name) { put("name", name); }

    public String getPollName() { return (String) get("name"); }


}
