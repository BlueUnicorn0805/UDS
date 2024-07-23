package hawaiiappbuilders.c.model;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;

import java.util.Calendar;

import hawaiiappbuilders.c.AppSettings;
import hawaiiappbuilders.c.R;
import hawaiiappbuilders.c.utils.ViewUtil;


public class ApptUtil {

    public static final int NEW_APPOINTMENT = 2220; // 2200
    public static final int RESCHEDULE_APPOINTMENT = 2225; // update the date and time
    public static final int ACCEPT_APPOINTMENT = 2230;
    public static final int CANCEL_APPOINTMENT = 2235;
    public static final int DECLINE_APPOINTMENT = 2240; // ????

    public interface OnUpdateApptListener {
        void onAppointmentDeclined(int newStatusId);

        void onAppointmentAccepted(int newStatusId);

        void onAppointmentRescheduled(int newStatusId);

        void onAppointmentCancelled(int newStatusId);
    }



    Context context;
    private OnUpdateApptListener listener;

    public ApptUtil(Context context) {
        this.context = context;
    }

    /**
     * From doctor to patient.LOGGED_IN
     */
    public boolean isApptForPatient(CalendarData.Data data) {
        AppSettings appSettings = new AppSettings(context);
        //return data.getNick() != appSettings.getUserId() && data.getBed() == appSettings.getUserId();
        return data.getClientID() != appSettings.getUserId() && data.getClientID() == appSettings.getUserId();
    }

    /**
     * From doctor.LOGGED_IN to other user
     */
    public boolean isApptCreatedByDoctor(CalendarData.Data data) {
        AppSettings appSettings = new AppSettings(context);
        //return data.getNick() == appSettings.getUserId() && (data.getBed() != appSettings.getUserId() && data.getBed() != 0);
        return data.getClientID() == appSettings.getUserId() && (data.getClientID() != appSettings.getUserId() && data.getClientID() != 0);
        //                     doctor                                            not user                                        set
    }

    /**
     * For user.LOGGED_IN is the doctor and patient
     */
    public boolean isApptCreatedByDoctorAndYouAreTheAttendee(CalendarData.Data data) {
        AppSettings appSettings = new AppSettings(context);
        //return data.getNick() == appSettings.getUserId() && data.getBed() == appSettings.getUserId();
        return data.getClientID() == appSettings.getUserId() && data.getClientID() == appSettings.getUserId();
    }

    /**
     * Load page buttons according to appointment status and attendeeMLID value
     *
     * If attendeeMLID != 0, show Cancel, hide Delete
     * Otherwise, hide Cancel, show Delete
     */
    public void initializeActionButtons(CalendarData.Data data,
                                        Button buttonAddAsNewContact,
                                        Button accept,
                                        Button cancel,
                                        Button decline,
                                        View update,
                                        View edit,
                                        View menu) {
        /*
        // Button "Add as New Contact"
        if(data.getClientID() != 0) {
            buttonAddAsNewContact.setVisibility(View.GONE);
            // Update, Edit, Menu
            if(isApptCreatedByDoctor(data)) {
                //ViewUtil.setVisible(update);
                ViewUtil.setVisible(edit);
                ViewUtil.setVisible(menu);
            } else if(isApptForPatient(data)) {
                //ViewUtil.setGone(update);
                ViewUtil.setGone(edit);
                ViewUtil.setGone(menu);
            } else if(isApptCreatedByDoctorAndYouAreTheAttendee(data)) {
                //ViewUtil.setVisible(update);
                ViewUtil.setVisible(edit);
                ViewUtil.setVisible(menu);
            }

            // Accept, Cancel, Decline
            if(isApptCreatedByDoctor(data)) {
                switch (data.getMode()) {
                    case NEW_APPOINTMENT:
                    case RESCHEDULE_APPOINTMENT:
                    case ACCEPT_APPOINTMENT:
                    case DECLINE_APPOINTMENT:
                        ViewUtil.setGone(accept);
                        ViewUtil.setVisible(cancel);
                        ViewUtil.setGone(decline);
                        break;
                    case CANCEL_APPOINTMENT:
                        ViewUtil.setGone(accept);
                        ViewUtil.setGone(cancel);
                        ViewUtil.setGone(decline);
                        break;
                }
            } else if(isApptForPatient(data)) {
                switch (data.getMode()) {
                    case NEW_APPOINTMENT:
                    case RESCHEDULE_APPOINTMENT:
                        ViewUtil.setVisible(accept);
                        ViewUtil.setVisible(cancel);
                        ViewUtil.setVisible(decline);
                        break;
                    case ACCEPT_APPOINTMENT:
                        ViewUtil.setGone(accept);
                        ViewUtil.setVisible(cancel);
                        ViewUtil.setGone(decline);
                        break;
                    case CANCEL_APPOINTMENT:
                    case DECLINE_APPOINTMENT:
                        ViewUtil.setGone(accept);
                        ViewUtil.setGone(cancel);
                        ViewUtil.setGone(decline);
                        break;
                }
            } else if(isApptCreatedByDoctorAndYouAreTheAttendee(data)) {
                ViewUtil.setGone(accept);
                ViewUtil.setGone(cancel);
                ViewUtil.setGone(decline);
            }
        } else {
            buttonAddAsNewContact.setVisibility(View.VISIBLE);
            ViewUtil.setGone(accept);
            ViewUtil.setGone(cancel);
            ViewUtil.setGone(decline);
            //ViewUtil.setVisible(update);
            ViewUtil.setVisible(edit);
            ViewUtil.setVisible(menu);
        }*/
    }

    /**
     * This will color the appointment itemView in the calendarView based on the returned appointment status
     */
    public static void colorItemByStatus(Context context, int status, TextView textView, CalendarData.Data calData) {
        /*
        ApptUtil apptUtil = new ApptUtil(context);
        if (calData.getBed() == 0) {
            if (status == ACCEPT_APPOINTMENT) {
                textView.setBackground(AppCompatResources.getDrawable(context, R.drawable.status_green));
            } else if(status == CANCEL_APPOINTMENT) {
                textView.setTextColor(Color.WHITE);
                textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                textView.setBackground(AppCompatResources.getDrawable(context, R.drawable.status_red));
            } else if(status == DECLINE_APPOINTMENT) {
                textView.setBackground(AppCompatResources.getDrawable(context, R.drawable.status_red));
            } else {
                textView.setBackground(AppCompatResources.getDrawable(context, R.drawable.status_default));
            }
        } else {
            if(apptUtil.isApptForPatient(calData)) {
                switch (status) {
                    case NEW_APPOINTMENT:
                        textView.setBackground(AppCompatResources.getDrawable(context, R.drawable.status_yellow));
                        break;
                    case RESCHEDULE_APPOINTMENT:
                        textView.setBackground(AppCompatResources.getDrawable(context, R.drawable.status_orange));
                        break;
                    case ACCEPT_APPOINTMENT:
                        textView.setBackground(AppCompatResources.getDrawable(context, R.drawable.status_green));
                        break;
                    case CANCEL_APPOINTMENT:
                        textView.setTextColor(Color.WHITE);
                        textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        textView.setBackground(AppCompatResources.getDrawable(context, R.drawable.status_red));
                        break;
                    case DECLINE_APPOINTMENT:
                        textView.setBackground(AppCompatResources.getDrawable(context, R.drawable.status_red));
                        break;
                }
            } else if(apptUtil.isApptCreatedByDoctor(calData)) {
                switch (status) {
                    case NEW_APPOINTMENT:
                        textView.setBackground(AppCompatResources.getDrawable(context, R.drawable.status_default));
                        break;
                    case RESCHEDULE_APPOINTMENT:
                        textView.setBackground(AppCompatResources.getDrawable(context, R.drawable.status_orange));
                        break;
                    case ACCEPT_APPOINTMENT:
                        textView.setBackground(AppCompatResources.getDrawable(context, R.drawable.status_green));
                        break;
                    case CANCEL_APPOINTMENT:
                        textView.setTextColor(Color.WHITE);
                        textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        textView.setBackground(AppCompatResources.getDrawable(context, R.drawable.status_red));
                        break;
                    case DECLINE_APPOINTMENT:
                        textView.setBackground(AppCompatResources.getDrawable(context, R.drawable.status_red));
                        break;
                }
            } else {
                if (status == ACCEPT_APPOINTMENT) {
                    textView.setBackground(AppCompatResources.getDrawable(context, R.drawable.status_green));
                } else if(status == CANCEL_APPOINTMENT) {
                    textView.setTextColor(Color.WHITE);
                    textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    textView.setBackground(AppCompatResources.getDrawable(context, R.drawable.status_red));
                } else if(status == DECLINE_APPOINTMENT) {
                    textView.setBackground(AppCompatResources.getDrawable(context, R.drawable.status_red));
                } else {
                    textView.setBackground(AppCompatResources.getDrawable(context, R.drawable.status_default));
                }
            }
        }*/
    }

    /*public String getProposalTime(CalendarData.Data apptData) {
        Calendar calStartTime = Calendar.getInstance();
        Calendar calEndTime = Calendar.getInstance();

        calStartTime.setTimeInMillis(DateUtil.parseDataFromFormat20(apptData.getStartDate()).getTime());
        calEndTime.setTimeInMillis(DateUtil.parseDataFromFormat20(apptData.getEndDate()).getTime());
        return String.format("%s - %s",
                DateUtil.toStringFormat_37(calStartTime.getTime()),
                DateUtil.toStringFormat_37(calEndTime.getTime()));

    }*/

    public boolean isDateTimeUpdated(Calendar start, Calendar end, Calendar dStart, Calendar dEnd) {
        return start.getTimeInMillis() != dStart.getTimeInMillis() || end.getTimeInMillis() != dEnd.getTimeInMillis();
    }
}
