package com.kevrain.consensus.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseRelation;

/**
 * Created by kfarst on 8/19/16.
 */
@ParseClassName("Location")
public class Location extends ParseObject {
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

    public void setPoll(Poll poll) {
        put("poll", poll);
    }

    public Poll getPoll() {
        return (Poll) getParseObject("poll");
    }

    public void setTitle(String title) {
        put("title", title);
    }
    public String getTitle() {
        return (String) get("title");
    }
}
