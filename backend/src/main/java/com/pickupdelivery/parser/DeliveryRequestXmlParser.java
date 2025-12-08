package com.pickupdelivery.parser;

import org.w3c.dom.Element;
import com.pickupdelivery.model.Demand;
import com.pickupdelivery.factory.DemandFactory;

public class DeliveryRequestXmlParser {
    // ...existing code...

    private Demand parseDemand(Element demandElement) {
        String id = demandElement.getAttribute("id");
        String pickupNodeId = demandElement.getAttribute("pickupNodeId");
        String deliveryNodeId = demandElement.getAttribute("deliveryNodeId");
        int pickupDurationSec = Integer.parseInt(demandElement.getAttribute("pickupDurationSec"));
        int deliveryDurationSec = Integer.parseInt(demandElement.getAttribute("deliveryDurationSec"));
        String courierId = demandElement.getAttribute("courierId");
        return DemandFactory.createDemand(id, pickupNodeId, deliveryNodeId, pickupDurationSec, deliveryDurationSec, courierId);
    }

    // ...existing code...
}
