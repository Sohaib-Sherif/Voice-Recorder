package ly.sohaib.voicerecorder;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RecordUtils {

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);
    private final MediaMetadataRetriever retriever = new MediaMetadataRetriever();
    private static File musicDirectory;
    private List<Record> records;

    RecordUtils(Context context){
        musicDirectory = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
    }

    /**
     *
     * @return An array whose contents are all the files in the app's music directory, otherwise if there are
     * no files inside the directory returns an empty array.
     */
    private File[] getFiles(){
        //probably should make a check if the media is mounted somewhere.something like a method.
        if (musicDirectory != null) {
            File[] musicFiles = musicDirectory.listFiles();
            Arrays.sort(musicFiles);
            return musicFiles;
        } else return new File[]{};
    }

    public List<Record> getRecords(){
        File[] RecordFiles = getFiles();
        records = new ArrayList<>(RecordFiles.length);
        for (int i = 0; i < RecordFiles.length; i++) {
            records.add(newRecord(RecordFiles[i]));
        }
        return records;
    }

    private Record newRecord(File recordFile){
        Record record = new Record();
        record.setName(recordFile.getName());
        retriever.setDataSource(recordFile.getAbsolutePath());
        record.setLength(Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
        record.setFilePath(recordFile.getAbsolutePath());
        return record;
    }

    public int count(){
        return records.size();
    }

    public static File generateNewRecordFile(){
        String date = simpleDateFormat.format(Calendar.getInstance().getTime());
        return new File(musicDirectory,date+".mp3");
    }

    public List<Record> updateRecords(){
        File[] updatedMusicFiles = getFiles();
        //this gets the last file in the array
        File theNewlyAddedFile = updatedMusicFiles[updatedMusicFiles.length - 1];
        records.add(newRecord(theNewlyAddedFile));
        return records;
    }

}
