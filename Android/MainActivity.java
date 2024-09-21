public class MainActivity extends AppCompatActivity {
    private boolean isPaused = true;
    private long elapsedTime = 0;
    private long startTimeInMillis;
    private Handler handler;
    private Runnable runnable;

    private TextView elapsedTimeText;
    private Button startPauseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        elapsedTimeText = findViewById(R.id.elapsedTimeText);
        startPauseButton = findViewById(R.id.startPauseButton);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (!isPaused) {
                    elapsedTime = System.currentTimeMillis() - startTimeInMillis;
                    updateTimerUI();
                    updateNotification();
                }
                handler.postDelayed(this, 1000); // Update every second
            }
        };

        startPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPaused) {
                    startTimer();
                    startPauseButton.setText("Pause");
                } else {
                    pauseTimer();
                    startPauseButton.setText("Resume");
                }
            }
        });

        createNotificationChannel();
    }

    private void startTimer() {
        if (isPaused) {
            startTimeInMillis = System.currentTimeMillis() - elapsedTime;
            isPaused = false;
            startForegroundService();
            handler.post(runnable);
        }
    }

    private void pauseTimer() {
        if (!isPaused) {
            isPaused = true;
        }
    }

    private void updateTimerUI() {
        elapsedTimeText.setText(getFormattedElapsedTime(elapsedTime));
    }

    private String getFormattedElapsedTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void startForegroundService() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, "TimerServiceChannel")
                .setContentTitle("Timer Running")
                .setContentText("Elapsed Time: " + getFormattedElapsedTime(elapsedTime))
                .setSmallIcon(R.drawable.ic_timer)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();

        startForeground(1, notification);
    }

    private void updateNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this, "TimerServiceChannel")
                .setContentTitle("Timer Running")
                .setContentText("Elapsed Time: " + getFormattedElapsedTime(elapsedTime))
                .setSmallIcon(R.drawable.ic_timer)
                .setOngoing(true)
                .build();

        notificationManager.notify(1, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "TimerServiceChannel",
                    "Timer Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}
