package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TripDocuments {

    @SerializedName("trip_documents")
    @Expose
    public List<TripDocument> tripDocuments = null;

}
