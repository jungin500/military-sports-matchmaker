package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;

public class NotificationExamActivity extends AppCompatActivity implements OnClickListener {

    private Button notify_a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_exam);

        notify_a = (Button) findViewById(R.id.Notify);
        notify_a.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.Notify){
            Intent intent = new Intent(this, MatchCompleteActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);
            NotificationCompat.Builder notifybuilder = new NotificationCompat.Builder(this);
            notifybuilder.setContentTitle("매칭이 완료되었습니다.");
            notifybuilder.setContentText("알림을 눌러 매칭 정보를 확인하세요");
            notifybuilder.setSmallIcon(R.mipmap.ic_launcher);
            notifybuilder.setAutoCancel(true);
            notifybuilder.setWhen(System.currentTimeMillis());
            notifybuilder.setContentIntent(pendingIntent);
            notifybuilder.setPriority(NotificationCompat.PRIORITY_MAX);


            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(123456, notifybuilder.build());
        }
    }
}
