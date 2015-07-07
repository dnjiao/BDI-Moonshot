package api;

import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;

public class PushFile {
	public static void main(String[] args) {
		int status = 500;
		PostMethod filePost = null;
		try {
			//From Directory or File we need to pick files
			File f = new File("/Users/djiao/Work/moonshot/1.vcf");
			//Need to validate the file name from the file name
			filePost = new PostMethod("http://10.111.100.207:8098/bdi/serviceingestion?domain=vcf");
			RequestEntity re = new FileRequestEntity(f,	"application/octet-stream");
			filePost.setRequestEntity(re);
			HttpClient client = new HttpClient();
			long start = System.currentTimeMillis();
			status = client.executeMethod(filePost);
			System.out.println("Time taken for execution " + (System.currentTimeMillis() - start) + "ms");
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		finally {
			filePost.releaseConnection();
		}

	}
	
}