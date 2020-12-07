package ly.sohaib.voicerecorder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class SavedRecordsAdapter extends RecyclerView.Adapter<SavedRecordsAdapter.RecordingsViewHolder> {

    private RecordUtils recordUtils;
    private List<Record> records;
    private Context context;
    private LinearLayoutManager linearLayout;

    SavedRecordsAdapter(RecordUtils recordUtils, Context context, LinearLayoutManager linearLayout){
        this.recordUtils = recordUtils;
        this.context = context;
        this.linearLayout = linearLayout;
        records = recordUtils.getRecords();
        RecordsObserver observer = new RecordsObserver(context, this);
        observer.startWatching();

    }

    @Override
    public RecordingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cardView = (CardView)LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view,parent,false);
        return new RecordingsViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(RecordingsViewHolder holder, int position) {
        CardView cardView = holder.cardView;
        ImageButton playButton = cardView.findViewById(R.id.play_button);
        ImageButton shareButton = cardView.findViewById(R.id.share_button);
        TextView fileName = cardView.findViewById(R.id.file_name);
        TextView fileLength = cardView.findViewById(R.id.file_length);

        Record record = records.get(position);
        if (record != null){
            fileName.setText(record.getName());
            long length = record.getLength();
            long minutes = TimeUnit.MILLISECONDS.toMinutes(length);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(length) - TimeUnit.MINUTES.toSeconds(minutes);
            fileLength.setText(String.format(Locale.US,"%02d:%02d", minutes, seconds));
        }

        playButton.setOnClickListener(e -> playIntent(position));
        shareButton.setOnClickListener(e -> shareIntent(position));


    }

    @Override
    public int getItemCount() {
        return recordUtils.count();
    }

    private void playIntent(int position) {
        Intent playIntent = new Intent();
        playIntent.setAction(Intent.ACTION_VIEW);
        playIntent.setDataAndType(Uri.fromFile(new File(records.get(position).getFilePath())),"audio/mp3");
        context.startActivity(Intent.createChooser(playIntent, context.getText(R.string.play_with)));
    }

    private void shareIntent(int position) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(records.get(position).getFilePath())));
        shareIntent.setType("audio/mp3");
        context.startActivity(Intent.createChooser(shareIntent, context.getText(R.string.send_to)));
    }

    private void  addedFile(){
        records = recordUtils.updateRecords();
        //linearLayout.scrollToPosition(records.size()-1);
        notifyItemInserted(records.size()-1);
    }


    static class RecordingsViewHolder extends RecyclerView.ViewHolder{

        private CardView cardView;

        RecordingsViewHolder(CardView itemView) {
            super(itemView);
            cardView = itemView;
        }
    }

    private static class RecordsObserver extends FileObserver{

        SavedRecordsAdapter adapter;
        RecordsObserver(Context context, SavedRecordsAdapter adapter){
            super(Objects.requireNonNull(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)).getAbsolutePath());
            this.adapter = adapter;

        }
        @Override
        public void onEvent(int event, @Nullable String path) {
            if (path == null) return;
            switch (event) {
                case FileObserver.CREATE:
                case FileObserver.CLOSE_WRITE:
                    adapter.addedFile();
                    break;
            }
        }
    }
}
