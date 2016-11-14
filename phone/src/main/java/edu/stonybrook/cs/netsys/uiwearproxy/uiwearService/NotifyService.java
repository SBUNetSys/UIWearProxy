package edu.stonybrook.cs.netsys.uiwearproxy.uiwearService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.widget.RemoteViews;

import com.orhanobut.logger.Logger;

import edu.stonybrook.cs.netsys.uiwearproxy.PhoneActivity;
import edu.stonybrook.cs.netsys.uiwearproxy.R;

public class NotifyService extends NotificationListenerService {

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Logger.i("notify connected");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Logger.i("notify removed");
        super.onNotificationRemoved(sbn);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        Logger.d("onNotificationPosted");

        Notification notification = sbn.getNotification();

        String packageName = sbn.getPackageName();
        long time = sbn.getPostTime();
        String title = "" + notification.extras.getCharSequence(Notification.EXTRA_TITLE);
        String text = "" + notification.extras.getCharSequence(Notification.EXTRA_TEXT);

        // TODO: 10/30/16 Sunday explore NMS notifyPostedLocked remote view info for customized
        //  notifications, for reading see https://github.com/KeithYokoma/RemoteViewsReader

        // FIXME: 10/30/16 Sunday see why bitmap is lost from system_server to this service
        // TODO: 10/30/16 Sunday  add wearableExtender if can retrieve all notifications data
        if ("com.spotify.music".equals(packageName)) {

            RemoteViews contentView = sbn.getNotification().contentView;
            Logger.i(contentView.getPackage());
//            final LayoutInflater layoutInflater =
//                    (LayoutInflater) getApplicationContext().getSystemService(
//                            Context.LAYOUT_INFLATER_SERVICE);
//            final ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(
//                    contentView.getLayoutId(), null);
//            contentView.reapply(getApplicationContext(), viewGroup);
//            final ImageView titleView = (ImageView) viewGroup.findViewById(R.id.coverart);
//            if (titleView != null) {
//                title = String.valueOf(titleView.getText());
//            }

//            RemoteViews remoteViews = sbn.getNotification().contentView;
//            RemoteViewsInfo info = RemoteViewsReader.read(this, remoteViews);
//            for (RemoteViewsAction action : info.getActions()) {
//                Logger.v("action: " + action.getActionName() + " id: " + action.getViewId());
//                if (action instanceof ReflectionAction) {
//                    Logger.v("ReflectionAction: " + action.getActionName() + " method: "
//                            + ((ReflectionAction) action).getMethodName());
//
//                    if (((ReflectionAction) action).getMethodName().equals("setImageResource")) {
//                        int iconID = (int) ((ReflectionAction) action).getValue();
//                        Logger.i("icon id" + iconID);
//
//                        try {
//                            Context context = createPackageContext(packageName,
//                                        CONTEXT_IGNORE_SECURITY);
//                            Drawable drawable = context.getResources().getDrawable(iconID);
//                            createNotification(packageName, contentView);
//
//                            Bitmap bmp = ((BitmapDrawable) drawable).getBitmap();
//                            if (bmp != null) {
//                                Logger.i("bitmap size" + bmp.getByteCount() / 1024);
//                                FileUtil.storeBitmapAsync(bmp, "UIWearNotify",
//                                        packageName + SystemClock.currentThreadTimeMillis());
//                            } else {
//                                Logger.e("bitmap null");
//                            }
//
//                        } catch (PackageManager.NameNotFoundException e) {
//                            e.printStackTrace();
//                        }
//
//
//                    }
//                }
//
//            }

        }
//        notification.getSmallIcon();

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.i("notify started");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return super.onBind(intent);
    }

    private void createNotification(String packageName, RemoteViews content) {

        Intent intent = new Intent(this, PhoneActivity.class);
        intent.putExtra("packageName", packageName);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Notification.Builder notifyBuilder = new Notification.Builder(this)
                .setWhen(System.currentTimeMillis())
                .setContent(content)
                .setContentIntent(contentIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS);


        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Notification notification = notifyBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        nm.notify(R.string.app_name, notification);
    }


    private void createNotification(String packageName, String title, String text,
            int icon) {

        Intent intent = new Intent(this, PhoneActivity.class);
        intent.putExtra("packageName", packageName);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Notification.Builder notifyBuilder = new Notification.Builder(this)
                .setSmallIcon(icon)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(": " + title)
                .setContentText(": " + text)
                .setContentIntent(contentIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS);


        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Notification notification = notifyBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        nm.notify(R.string.app_name, notification);
    }
}
