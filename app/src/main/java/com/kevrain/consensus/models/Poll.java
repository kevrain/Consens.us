package com.kevrain.consensus.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseRelation;


import java.util.Set;

/**
 * Created by kfarst on 8/19/16.
 */
@ParseClassName("Poll")
public class Poll extends ParseObject {
    public boolean pinned;

    public Poll() {}

    public ParseRelation<Vote> getVotesRelation () {
        return getRelation("vote");
    }

    public void setGroup(Group group) {
        put("group", group);
    }

    public Group getGroup() {
        return (Group) getParseObject("group");
    }

    public void addPollOption(PollOption pollOption) {
        getPollOptionRelation().add(pollOption);
        saveInBackground();
    }

    public ParseRelation<PollOption> getPollOptionRelation () {
        return getRelation("pollOption");
    }

    public void removePollOption(PollOption pollOption) {
        getPollOptionRelation().remove(pollOption);
        saveInBackground();
    }

    public void removePollOptions(Set<PollOption> pollOptions) {
        for (PollOption option: pollOptions) {
            getPollOptionRelation().remove(option);
        }
        saveInBackground();
    }

    public void setPollName(String name) { put("name", name); }

    public String getPollName() { return (String) get("name"); }
}
