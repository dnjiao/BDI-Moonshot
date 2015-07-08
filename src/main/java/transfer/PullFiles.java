package transfer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PullFiles {
	
	final static String DEST_ROOT = "/rsrch1/rists/moonshot";
	
	public static void main(String[] args) {
		final String type = System.getenv("TYPE");
	    final String mode = System.getenv("MODE");
	    final String destPath = DEST_ROOT + "/" + type;
	    final String logPath = destPath + "/logs";
	    File destDir = new File(destPath);
	    if (!destDir.exists()){
	        System.err.println("ERROR: Destination path " + destPath + " does not exist.");
	        System.exit(1);
	    }
	    File logDir = new File(logPath);
	    if (!logDir.exists()) {
	    	Files.createDirectory(Paths.get(logPath));
	    }
	    if not os.path.exists(log_path):
	        os.system('mkdir log_path')
	    #open log file to write
	    log_time = dt.datetime.now().strftime('%m%d%Y%H%M%S')
	    log_file = open(os.path.join(log_path, 'tmp.log'), 'a')
	    line_count = 0
	    for i in range(1, 3):
	        source_dir = os.getenv('SOURCE_DIR' + str(i))
	        if source_dir != '':
	            line_count = line_count + cpFiles(source_dir, dest, type, 'cp', mode, log_file)
	    log_file.close()
	    if line_count == 0:
	        os.system('rm %s' % os.path.join(log_path, 'tmp.log'))
	    else:
	        os.system('mv %s %s' % (os.path.join(log_path, 'tmp.log'), os.path.join(log_path, log_time + '.log')))
	}
	
	public static int cpFiles(source, dest, type, protocol, mode, log) {
	    if protocol == 'cp':
	        cmd = 'cp'
	    if protocol == 'rsync':
	        cmd = 'rsync -auv';
	    if type == 'vcf':
	        for root, dirs, files in os.walk(source):
	            for f in files:
	                if f.endswith('.vcf'):
	                    if mode == 'Update all':
	                        source_file = os.path.join(root, f)
	                        cmd_str = cmd + ' ' + source_file + ' ' + dest
	                        print cmd_str
	                        os.system(cmd_str)
	                        log.write("%s\t%s\t%s\t%s\n" % (f, root, dest, protocol))
	                        return 1
	                    else:
	                        last_time = get_last_log_date(os.path.join(dest, 'logs'))
	                        file_gtime = dt.datetime.fromtimestamp(os.path.getmtime(os.path.join(root, f)))
	#                       print f, file_gtime, last_time, (file_gtime > last_time)
	                        if file_gtime > last_time:
	                            source_file = os.path.join(root, f)
	                            cmd_str = cmd + ' ' + source_file + ' ' + dest
	                            print cmd_str
	                            os.system(cmd_str)
	                            log.write("%s\t%s\t%s\t%s\n" % (f, root, dest, protocol))
	                            return 1
	    return 0

	}
	
	def get_last_log_date(dir):
	    last = '01012001_010101'
	    last_dt = str2dt(last)
	    for f in os.listdir(dir):
	        if f.endswith('.log') and f != 'tmp.log':
	            filetime = f.split('.')[0]
	            if str2dt(filetime) > last_dt:
	                last_dt = str2dt(filetime)
	    return last_dt

	# convert String to datetime
	def str2dt(str):
	    return dt.datetime.strptime(str, "%m%d%Y%H%M%S")
}