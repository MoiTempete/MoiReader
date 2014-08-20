package com.shixforever.reader.adapter;

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.shixforever.reader.R;
import com.shixforever.reader.data.BookFile;
import com.shixforever.reader.utils.FileTools;

public class AlbumShelfAdapter extends BaseAdapter {

    private List<BookFile> albums;

    private Context context;

    public AlbumShelfAdapter(List<BookFile> albums, Context context) {
        super();
        this.albums = albums;
        this.context = context;
    }

    @Override
    public int getCount() {
        return albums.size();
    }

    @Override
    public Object getItem(int position) {
        return albums.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView tvAlbumName;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.album_item, null);
        }
        tvAlbumName = (TextView) convertView.findViewById(R.id.tvAlbumName);
        BookFile album = albums.get(position);
        if (album.flag.equals("1")) {
            tvAlbumName.setBackgroundResource(R.drawable.cover_txt);
            tvAlbumName.setText(album.name);
        } else if (album.cover.equals("0")) {
            tvAlbumName.setBackgroundResource(R.drawable.cover_txt);
            tvAlbumName.setText(album.name);
        } else {
            tvAlbumName.setBackgroundResource(FileTools.getResource(context,
                    album.cover));
        }
        return convertView;
    }

    public void change(List<BookFile> album) {
        this.albums = album;
        notifyDataSetChanged();
    }
}
