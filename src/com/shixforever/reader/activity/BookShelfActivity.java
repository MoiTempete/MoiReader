package com.shixforever.reader.activity;

import java.io.File;
import java.util.List;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.Toast;
import com.shixforever.reader.R;
import com.shixforever.reader.adapter.AlbumShelfAdapter;
import com.shixforever.reader.data.BookFile;
import com.shixforever.reader.manager.FileManager;
import com.shixforever.reader.utils.FusionField;
import com.shixforever.reader.utils.Tools;

import com.shixforever.reader.db.DBManager;

public class BookShelfActivity extends BaseActivity {
	private GridView bookShelf;
	private AlbumShelfAdapter albumShelfAdapter;
	private DBManager mgr;
	private List<BookFile> books;
	/**
	 * book File cache
	 */
	private File filecatch;
	private String filecatchpath = "/data/data/"
			+ FusionField.baseActivity.getPackageName() + "/";
	private boolean isConnected;
	/**
	 * 积分墙
	 */
	private TextView mTextViewPoints;
	private boolean nhFlag = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		isConnected = isWifiConnect();

		// 预加载
		initView();
		mgr = new DBManager(this);
		initData();
		mTextViewPoints = (TextView) findViewById(R.id.tv_points);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onRestart() {
		this.notifyDataChange();
		super.onRestart();
	}

	private void initView() {
		bookShelf = (GridView) findViewById(R.id.bookShelf);
	}

	private void notifyDataChange() {
	}

	private void initData() {
		// initialize DB
		books = mgr.queryPro();
		if (!nhFlag) {
			for (int i = 0; i < books.size(); i++) {
				if (books.get(i).flag.equals("3"))
					books.remove(i);
			}
		}
		File file = new File(FileManager.FILE_SDCARD_PATH);
		if (file.isDirectory()) {
			File[] fileArray = file.listFiles();
			if (null != fileArray && 0 != fileArray.length) {
                for (File aFileArray : fileArray) {
                    BookFile bookFile = new BookFile();
                    bookFile.path = aFileArray.getPath();
                    bookFile.name = aFileArray
                            .getPath()
                            .trim()
                            .substring(
                                    aFileArray.getPath().trim()
                                            .lastIndexOf("/") + 1);
                    bookFile.flag = "1";
                    books.add(bookFile);
                }

			}
		}
		albumShelfAdapter = new AlbumShelfAdapter(books, this);
		bookShelf.setAdapter(albumShelfAdapter);
		bookShelf.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				BookFile albumitem = (BookFile) parent
						.getItemAtPosition(position);
				// 传递file会崩溃
				BookFile album = new BookFile();
				album.id = albumitem.id;
				album.name = albumitem.name;
				album.cover = albumitem.cover;
				album.path = albumitem.path;
				album.flag = albumitem.flag;
				System.out.println(filecatchpath);
				if (albumitem.flag.equals("0")) {
					filecatch = Tools.getFile(albumitem.file, filecatchpath,
                            "catch.txt");
					if (filecatch == null) {
						System.err.println("null");
					}
				}
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putSerializable("path", album);
				intent.putExtras(bundle);
				intent.setClass(BookShelfActivity.this, BookActivity.class);
				startActivity(intent);
			}
		});
		bookShelf.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(final AdapterView<?> parent,
					View view, final int position, long id) {
				AlertDialog.Builder builder = new Builder(
						BookShelfActivity.this);
				builder.setMessage("删除");
				builder.setTitle("确认删除？");
				builder.setPositiveButton("确认",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								BookFile bookFile = (BookFile) parent
										.getItemAtPosition(position);

								if (bookFile.flag.equals("0")) {
									// 删除
									mgr.deleteBook((BookFile) parent
											.getItemAtPosition(position));
								} else {
									File file = new File(
											FileManager.FILE_SDCARD_PATH
													+ bookFile.name);
									if (file.isFile()) {
										file.delete();
									}
								}
								// 刷新
								albumShelfAdapter.change(refresh());
								Toast.makeText(BookShelfActivity.this, "删除完成",
										Toast.LENGTH_SHORT).show();
							}
						});
				builder.setNegativeButton("取消",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						});
				builder.create().show();
				return true;
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mgr.closeDB();
	}

	/**
	 * 判断wifi是否连接
	 * 
	 * @return
	 */
	public boolean isWifiConnect() {
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return mWifi.isConnected();
	}

	private List<BookFile> refresh() {
		List<BookFile> booksdata = mgr.queryPro();
		if (!nhFlag) {
			for (int i = 0; i < booksdata.size(); i++) {
				if (booksdata.get(i).flag.equals("3"))
					booksdata.remove(i);
			}
		}
		File file = new File(FileManager.FILE_SDCARD_PATH);
		if (file.isDirectory()) {
			File[] fileArray = file.listFiles();
			if (null != fileArray && 0 != fileArray.length) {
				for (int i = 0; i < fileArray.length; i++) {
					BookFile bookFile = new BookFile();
					bookFile.path = fileArray[i].getPath();
					bookFile.name = fileArray[i]
							.getPath()
							.trim()
							.substring(
									fileArray[i].getPath().trim()
											.lastIndexOf("/") + 1);
					bookFile.flag = "1";
					booksdata.add(bookFile);
				}

			}
		}
		return booksdata;
	}
}