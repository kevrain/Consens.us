package com.kevrain.consensus.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseRelation;

/**
 * Created by kfarst on 8/19/16.
 */
@ParseClassName("PollOption")
public class PollOption extends ParseObject {
    public boolean pinned;

    public PollOption() {}

    public PollOption(String name, long date) {
        setName(name);
        setDate(date);
    }

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

    public void setName(String name) {
        put("name", name);
    }

    public String getName() {
        return (String) get("name");
    }

    public void setDate(long date) {
        put("date", date);
    }

    public long getDate() {
        return getLong("date");
    }
}
