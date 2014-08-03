package com.shixforever.reader.data;

import java.io.Serializable;

public class BookFile implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/***
	 * 
	 */
	public int id;
	/***
	 * 书籍名
	 */
	public String name;
	/***
	 * 文件流
	 */
	public byte[] file;
	/***
	 * 封面
	 */
	public String cover;
	/** 
	* @Fields path : 本地路径 
	*/ 
	public String path;
	/** 
	* @Fields flag : 类别 1 为SD卡
	*/ 
	public String flag="0";
	public BookFile() {
	}

}
