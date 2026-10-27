package com.example.grotrack;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.grotrack.data.GroTrackDatabase;
import com.example.grotrack.data.InventoryItem;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ExpiryCheckWorker extends Worker {
    public static final String KEY_TEST_RUN="testRun";
    public ExpiryCheckWorker(@NonNull Context context,@NonNull WorkerParameters parameters) { super(context,parameters); }

    @NonNull @Override public Result doWork() {
        try {
            List<InventoryItem> items=GroTrackDatabase.getInstance(getApplicationContext()).batchDao().getInventory();
            LocalDate today=LocalDate.now(); int expiring=0,expired=0; List<String> examples=new ArrayList<>();
            for(InventoryItem item:items) {
                if(item.archived||item.remainingQuantity<=0||item.expiryDate==null)continue;
                LocalDate expiry;
                try { expiry=LocalDate.parse(item.expiryDate); } catch(Exception ignored) { continue; }
                long days=ChronoUnit.DAYS.between(today,expiry);
                if(days<0) { expired++; addExample(examples,item.name+" (expired)"); }
                else if(days<=3) { expiring++; addExample(examples,item.name+(days==0?" (expires today)":" (expires in "+days+" day"+(days==1?"":"s")+")")); }
            }
            boolean testRun=getInputData().getBoolean(KEY_TEST_RUN,false);
            if(expiring+expired==0&&!testRun)return Result.success();
            if(!ExpiryNotifications.canNotify(getApplicationContext()))return Result.success();
            String title=expiring+expired==0?"GroTrack expiry check":"Groceries need attention";
            String message;
            if(expiring+expired==0)message="Test check completed. No active batches are expired or due within the next 3 days.";
            else {
                message=expiring+" expiring soon, "+expired+" expired with stock remaining.";
                if(!examples.isEmpty())message+=" "+String.join(", ",examples)+(expiring+expired>examples.size()?", and more.":".");
                if(testRun)message="Test check: "+message;
            }
            ExpiryNotifications.show(getApplicationContext(),title,message);
            return Result.success();
        } catch(Exception ex) { return Result.retry(); }
    }

    private void addExample(List<String> examples,String value) { if(examples.size()<3)examples.add(value); }
}
