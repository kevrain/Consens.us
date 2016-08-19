package com.kevrain.consensus.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

/**
 * Created by kfarst on 8/19/16.
 */
@ParseClassName("Group")
public class Group extends ParseObject {
    public void setOwner(ParseUser owner) {
        put("owner", owner);
    }

    public ParseUser getOwner() {
        return (ParseUser) getParseObject("owner");
    }

    public ParseRelation<ParseUser> getMembersRelation () {
        return getRelation("members");
    }

    public ParseRelation<Poll> getPollsRelation () {
        return getRelation("polls");
    }

    public void addMember(ParseUser user) {
        getMembersRelation().add(user);
        saveInBackground();
    }

    public void removeMember(ParseUser user) {
        getMembersRelation().remove(user);
        saveInBackground();
    }

    public void addPoll(Poll poll) {
        getPollsRelation().add(poll);
        saveInBackground();
    }

    public void removePoll(Poll poll) {
        getPollsRelation().remove(poll);
        saveInBackground();
    }

    public void setTitle(String title) {
        put("title", title);
    }
    public String getTitle() {
        return (String) get("title");
    }
}
