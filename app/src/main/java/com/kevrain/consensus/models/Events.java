package com.kevrain.consensus.models;

import com.parse.ParseObject;

/**
 * Created by shravyagarlapati on 8/19/16.
 */
public class Events extends ParseObject {

    String eventName;
    String eventImage;
    String invitedBy;

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setGetEventImage(String eventImage) {
        this.eventImage = eventImage;
    }

    public void setInvitedBy(String invitedBy) {
        this.invitedBy = invitedBy;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventImage() {
        return eventImage;
    }

    public String getInvitedBy() {
        return invitedBy;
    }


}
