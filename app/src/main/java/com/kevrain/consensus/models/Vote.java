package com.kevrain.consensus.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by kfarst on 8/19/16.
 */
@ParseClassName("Vote")
public class Vote extends ParseObject {
    public Vote() {}

    public void setUser(ParseUser user) {
        put("user", user);
    }

    // Get the user for this comment
    public ParseUser getUser()  {
        return getParseUser("user");
    }

    public void setPoll(Poll poll) {
        put("poll", poll);
    }

    // Get the poll for this comment
    public Poll getPoll()  {
        return (Poll) getParseObject("poll");
    }

    public void setPollOption(PollOption pollOption) {
        put("pollOption", pollOption);
    }

    public PollOption getPollOption() {
        return (PollOption) getParseObject("location");
    }
}
