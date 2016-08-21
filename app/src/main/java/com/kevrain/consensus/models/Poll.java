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
}
