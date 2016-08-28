package com.kevrain.consensus.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;


import java.util.Set;

/**
 * Created by kfarst on 8/19/16.
 */
@ParseClassName("Group")
public class Group extends ParseObject {

    public Group() {}

    public static String OWNER = "owner";
    public static String MEMBERS = "members";
    public static String POLLS = "polls";
    public static String TITLE = "title";

    public void setOwner(ParseUser owner) {
        put(OWNER, owner);
    }

    public ParseUser getOwner() {
        return (ParseUser) getParseObject(OWNER);
    }

    public ParseRelation<ParseUser> getMembersRelation () {
        return getRelation(MEMBERS);
    }

    public ParseRelation<Poll> getPollsRelation () {
        return getRelation(POLLS);
    }

    public void addMember(ParseUser user) {
        getMembersRelation().add(user);
        saveInBackground();
    }

    public void removeMember(ParseUser user) {
        getMembersRelation().remove(user);
        saveInBackground();
    }

    public void removeMember(Set<ParseUser> users) {
        ParseRelation membersRelation = getMembersRelation();
        for (ParseUser user: users) {
           membersRelation.remove(user);
        }
    }

    public void addPoll(Poll poll) {
        getPollsRelation().add(poll);
        saveInBackground();
    }

    public void removePoll(Poll poll) {
        getPollsRelation().remove(poll);
        saveInBackground();
    }

    public void addMembers(Set<ParseUser> members) {
        ParseRelation membersRelation = getMembersRelation();
        for (ParseUser member: members) {
            membersRelation.add(member);
        }
    }

    public void setTitle(String title) {
        put(TITLE, title);
    }

    public String getTitle() {
        return (String) get(TITLE);
    }
}
