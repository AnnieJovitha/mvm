package org.vaadin.mvm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.impl.nio.conn.PoolingClientAsyncConnectionManager;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.reactor.IOReactorException;

@WebServlet("/tiles/*")
public class TileProxy extends HttpServlet {

	private HttpAsyncClient httpclient;

	private static final File CACHE_DIR = new File(
			System.getProperty("user.home") + "/tmp/tilecache");
	static {
		CACHE_DIR.mkdirs();
	}

	Map<File, Long> lastCaches = Collections
			.synchronizedMap(new HashMap<File, Long>());

	int rqCounter = 1;
	private final int MAX_CACHEFILES = 30000;
	private final int MAINTAIN_CACHE = MAX_CACHEFILES / 2;

	@Override
	public void init() throws ServletException {
		super.init();
		readExistingCache(CACHE_DIR);
		try {
			httpclient = new DefaultHttpAsyncClient();
			PoolingClientAsyncConnectionManager connectionManager = (PoolingClientAsyncConnectionManager) httpclient
					.getConnectionManager();
			connectionManager.setDefaultMaxPerRoute(10);
			connectionManager.setMaxTotal(20);
			httpclient.start();
		} catch (IOReactorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void readExistingCache(File cacheDir) {
		File[] listFiles = cacheDir.listFiles();
		for (File file : listFiles) {
			if(file.isDirectory()) {
				readExistingCache(file);
			} else if (file.getName().endsWith(".png")) {
				lastCaches.put(file, file.lastModified());
			}
		}
	}

	@Override
	public void destroy() {
		try {
			httpclient.shutdown();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.destroy();
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		long currentTimeMillis = System.currentTimeMillis();

		File entry;
		try {
			entry = findCacheEntry(req.getPathInfo());
			if (entry.exists()) {
				serveFromCache(resp, entry);
			} else {
				Thread.sleep(1000);
				entry = findCacheEntry(req.getPathInfo());
				serveFromCache(resp, entry);
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}

		rqCounter++;
		if (rqCounter % (MAINTAIN_CACHE) == 0) {
			maintainCache();
		}
//		System.out.println("Request took"
//				+ (System.currentTimeMillis() - currentTimeMillis));
	}

	private File findCacheEntry(final String uri) throws IOException,
			ClientProtocolException, InterruptedException, ExecutionException {
		long now = System.currentTimeMillis();
		File file = new File(CACHE_DIR, uri);
		lastCaches.put(file, now);
		if (!file.exists()) {
			HttpGet request = new HttpGet("http://tile2.kartat.kapsi.fi" + uri);
			Future<HttpResponse> future = httpclient.execute(request, null);
			HttpResponse response = future.get();
			HttpEntity entity = response.getEntity();
			File f = File.createTempFile(uri.replace("/", "-"), "jpg");
			entity.writeTo(new FileOutputStream(f));
			// Convert to jpg, much smaller
			ImageIO.write(ImageIO.read(f), "jpg", f);
			file.getParentFile().mkdirs();
			f.renameTo(file);
//			System.out.println("Cache miss, cached in " +
//					+ (System.currentTimeMillis() - now));
		}
		return file;
	}

	private void maintainCache() throws IOException {
		new Thread() {
			@Override
			public void run() {
				synchronized (lastCaches) {
					long currentTimeMillis = System.currentTimeMillis();
					ArrayList<Entry<File, Long>> arrayList = new ArrayList<Entry<File, Long>>(
							lastCaches.entrySet());
					Collections.sort(arrayList,
							new Comparator<Entry<File, Long>>() {
								@Override
								public int compare(Entry<File, Long> o1,
										Entry<File, Long> o2) {
									return o1.getValue().compareTo(
											o2.getValue());
								}
							});
					int filesToDelete = lastCaches.size() - MAX_CACHEFILES;
					System.out.println("Maintaining tile cache, deleting "
							+ filesToDelete + "files");
					System.out.println("Total of " + rqCounter + " tiles served.");
					for (int i = 0; i < filesToDelete; i++) {
						Entry<File, Long> entry2 = arrayList.get(i);
						File key = entry2.getKey();
						Long removed = lastCaches.remove(key);
						key.delete();
//						System.out.println("Deleting cache for "
//								+ entry2.getKey() + ", last used "
//								+ new Date(removed));
					}
					System.out.println("Cache maintaining done in "
							+ (System.currentTimeMillis() - currentTimeMillis));
				}
			}
		}.start();

	}

	private void serveFromCache(HttpServletResponse resp, File entry)
			throws IOException {
		resp.setHeader("Cache-Control", "max-age=" + (3600 * 24));
		resp.setContentType("image/jpeg");
		FileInputStream input = new FileInputStream(entry);
		long size = input.getChannel().size();
		resp.setContentLength((int) size);
		IOUtils.copy(input, resp.getOutputStream());
		input.close();
	}

}
