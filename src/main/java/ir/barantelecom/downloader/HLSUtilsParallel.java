package ir.barantelecom.downloader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class HLSUtilsParallel {
  static ExecutorService executorService=new ThreadPoolExecutor(1, 4, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());;
  public HLSUtilsParallel() {

  }

  public static URL getHighestQualityM3u8Url(String masterM3u8Url, long delay) {
    BufferedReader br = null;

    try {
      masterM3u8Url = masterM3u8Url + "&delay=" + delay;
      System.out.println(masterM3u8Url);
      URL url = new URL(masterM3u8Url);
      URLConnection conn = url.openConnection();
      br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      ArrayList<String> lines = new ArrayList();

      String line;
      while((line = br.readLine()) != null) {
        System.out.println(line);
        lines.add(line);
      }

      List<Integer> bdwths = new ArrayList();
      HashMap<Integer, String> map = new HashMap();

      for(int i = 0; i < lines.size(); ++i) {
        String c = (String)lines.get(i);
        if (c.startsWith("#EXT-X-STREAM-INF")) {
          Integer bw = Integer.valueOf(c.substring(c.lastIndexOf("=") + 1));
          bdwths.add(bw);
          map.put(bw, lines.get(i + 1));
        }
      }

      Integer max = (Integer)bdwths.stream().max(Comparator.naturalOrder()).get();
      URL var27 = new URL((String)map.get(max));
      return var27;
    } catch (MalformedURLException var23) {
      var23.printStackTrace();
    } catch (IOException var24) {
      var24.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException var22) {
          var22.printStackTrace();
        }
      }

    }

    return null;
  }

  public static List<URL> getChunkUrlsFormM3u8(URL m3u8Url) {
    String baseUrl = getBaseUrl(m3u8Url);
    ArrayList chunks = new ArrayList();

    try {
      String wholeM3u8Str = IOUtils.toString(m3u8Url, Charset.forName("UTF-8"));
      List<String> lines = (List)Arrays.stream(wholeM3u8Str.split("\n")).collect(Collectors.toList());

      for(int i = 0; i < lines.size(); ++i) {
        String c = ((String)lines.get(i)).trim();
        if (!c.isEmpty() && !c.startsWith("#")) {
          chunks.add(new URL(baseUrl + "/" + c));
        }
      }
    } catch (IOException var7) {
      var7.printStackTrace();
    }

    return chunks;
  }

  public static List<Future<File>> downloadChunks(List<URL> chunksUrls, String destDir) {
    List<Future<File>> res = new ArrayList();
    Iterator var3 = chunksUrls.iterator();
    while(var3.hasNext()) {
      URL chunkUrl = (URL)var3.next();
        Callable<File> callableTask = () -> {
          String fileName = FilenameUtils.getName(chunkUrl.getPath());
          String newFilePath = destDir + File.separator + fileName;
          File newFile = new File(newFilePath);
          FileUtils.copyURLToFile(chunkUrl, newFile);
          return newFile;
        };
        res.add(executorService.submit(callableTask));
    }
    return res;
  }

  public static String getBaseUrl(URL url) {
    return url.getProtocol() + "://" + url.getAuthority();
  }

  public static void concatFiles(File destFile, List<File> files) {
    Iterator var2 = files.iterator();

    while(var2.hasNext()) {
      File file = (File)var2.next();

      try {
        FileUtils.writeByteArrayToFile(destFile, FileUtils.readFileToByteArray(file), true);
      } catch (IOException var5) {
        var5.printStackTrace();
      }
    }

  }

  public static void deleteFiles(List<File> files) {
    Iterator var1 = files.iterator();

    while(var1.hasNext()) {
      File file = (File)var1.next();

      try {
        FileUtils.forceDelete(file);
      } catch (IOException var4) {
        var4.printStackTrace();
      }
    }

  }

  public static void downloadStreamInNewThread(final String login, final String guid, final int channelId, final long delay, final int durationInMin, final File destFolder, final String targetFileName, final JProgressBar progressBar, final JButton startDownloadBtn) {
    (new Thread() {
      public void run() {
        while(!interrupted()) {
          String masterM3u8Url = PerceptionHttpDAO.getChannelUrl(login, guid, channelId, delay);
          URL m3u8Url = HLSUtilsParallel.getHighestQualityM3u8Url(masterM3u8Url, delay / 1000L);
          Set<URL> downloadedChunksURL = new HashSet();
          List<Future<File>> downloadedChunksFiles = new ArrayList();
          int i = 0;

          while(true) {
            List<URL> chunkUrls = HLSUtilsParallel.getChunkUrlsFormM3u8(m3u8Url);
            List<URL> newChunks = new ArrayList();
            Iterator var8 = chunkUrls.iterator();

            while(var8.hasNext()) {
              URL chunkUrl = (URL)var8.next();
              if (!downloadedChunksURL.contains(chunkUrl)) {
                newChunks.add(chunkUrl);
              }
            }

            if (!newChunks.isEmpty()) {
              List<Future<File>> chunkFiles = HLSUtilsParallel.downloadChunks(newChunks, Config.TMP_DIR_ADD);
              downloadedChunksFiles.addAll(chunkFiles);
              downloadedChunksURL.addAll(newChunks);
              Iterator var13 = newChunks.iterator();

              while(var13.hasNext()) {
                URL newChunk = (URL)var13.next();
                System.out.println(i + 1 + "\t" + FilenameUtils.getName(newChunk.getPath()) + "\t" + StringUtils.leftPad(String.valueOf(downloadedChunksFiles.size() * 10 / 60), 2, '0') + ":" + StringUtils.leftPad(String.valueOf(downloadedChunksFiles.size() * 10 % 60), 2, '0'));
              }
            }

            progressBar.setValue((int)((float)downloadedChunksFiles.size() / ((float)durationInMin * 6.0F + 1.0F) * 100.0F));
            if (downloadedChunksFiles.size() * 10 > durationInMin * 60) {
              if (!destFolder.exists()) {
                destFolder.mkdirs();
              }
              List<File> completedChunksFiles =
                  downloadedChunksFiles.parallelStream().map(fileFuture -> {
                    try {
                      return fileFuture.get();
                    } catch (InterruptedException e) {
                      e.printStackTrace();
                    } catch (ExecutionException e) {
                      e.printStackTrace();
                    }
                    return null;
                  }

                  ).collect(Collectors.toList());
              HLSUtilsParallel.concatFiles(new File(Config.DEST_DIR_ADD + File.separator + targetFileName + ".ts"), completedChunksFiles);
              HLSUtilsParallel.deleteFiles(completedChunksFiles);
              System.out.println("Download completed. '" + targetFileName + "'");
              JOptionPane.showMessageDialog((Component)null, "Download completed. '" + targetFileName + "'");
              startDownloadBtn.setEnabled(true);
              Thread.currentThread().interrupt();
              break;
            }

            try {
              Thread.sleep(100L);
            } catch (InterruptedException var11) {
              var11.printStackTrace();
            }

            ++i;
          }
        }

      }
    }).start();
  }

//  public static void main(String[] args) throws MalformedURLException, InterruptedException, PerceptionCommunicationProblemException {
//    int durationInMin = 2;
//    Calendar cal = Calendar.getInstance();
//    cal.set(11, 12);
//    cal.set(12, 30);
//    cal.set(13, 0);
//    Date fromDate = cal.getTime();
//    System.out.println("fromDate = " + fromDate);
//    long timeDiffInMillis = 0L;
//    timeDiffInMillis = PerceptionHttpDAO.getTimeDiffInMillis();
//    System.out.println("timeDiffInMillis = " + timeDiffInMillis);
//    String login = PerceptionHttpDAO.login("989126119588", "123456");
//    String guid = PerceptionHttpDAO.getProfileList(login);
//    Date date = new Date();
//    System.out.println("date = " + date);
//    long delay = date.getTime() - timeDiffInMillis - fromDate.getTime();
//    System.out.println("delay = " + delay);
//    String masterM3u8Url = PerceptionHttpDAO.getChannelUrl(login, guid, 36, delay);
//    System.out.println(masterM3u8Url);
//    URL m3u8Url = getHighestQualityM3u8Url(masterM3u8Url, delay / 1000L);
//    Set<URL> downloadedChunksURL = new HashSet();
//    List<File> downloadedChunksFiles = new ArrayList();
//    int i = 0;
//
//    while(true) {
//      List<URL> chunkUrls = getChunkUrlsFormM3u8(m3u8Url);
//      List<URL> newChunks = new ArrayList();
//      Iterator var18 = chunkUrls.iterator();
//
//      while(var18.hasNext()) {
//        URL chunkUrl = (URL)var18.next();
//        if (!downloadedChunksURL.contains(chunkUrl)) {
//          newChunks.add(chunkUrl);
//        }
//      }
//
//      if (!newChunks.isEmpty()) {
//        List<File> chunkFiles = downloadChunks(newChunks, Config.TMP_DIR_ADD);
//        downloadedChunksFiles.addAll(chunkFiles);
//        downloadedChunksURL.addAll(newChunks);
//        Iterator var22 = newChunks.iterator();
//
//        while(var22.hasNext()) {
//          URL newChunk = (URL)var22.next();
//          System.out.println(i + 1 + "\t" + FilenameUtils.getName(newChunk.getPath()) + "\t" + StringUtils.leftPad(String.valueOf(downloadedChunksFiles.size() * 10 / 60), 2, '0') + ":" + StringUtils.leftPad(String.valueOf(downloadedChunksFiles.size() * 10 % 60), 2, '0'));
//        }
//      }
//
//      if (downloadedChunksFiles.size() * 10 > durationInMin * 60) {
//        concatFiles(new File(Config.DEST_DIR_ADD + File.separator + System.currentTimeMillis() + ".ts"), downloadedChunksFiles);
//        deleteFiles(downloadedChunksFiles);
//        return;
//      }
//
//      Thread.sleep(10000L);
//      ++i;
//    }
//  }
}
