package org.mdacc.rists.bdi.db.utils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.mdacc.rists.bdi.vo.ConsumerFileReqVO;

import oracle.jdbc.OracleTypes;

public class ConsumerFileReqUtil {
	public static void main(String[] args) {
		
		List<ConsumerFileReqVO> list = getAllConsumer();
		
	}
	
	public static List<ConsumerFileReqVO> getAllConsumer() {
		Connection con = DBConnection.getConnection();
		CallableStatement stmt;
		ResultSet rs = null;
		List<ConsumerFileReqVO> consumerList = null;
		try {
			System.out.println("Calling procedure CONSUMER_FILE_REQ_UTIL.get_all");
			stmt = con.prepareCall("{call CONSUMER_FILE_REQ_UTIL.get_all(?,?,?,?)}");
			stmt.registerOutParameter(1, OracleTypes.CURSOR);
			stmt.registerOutParameter(2, Types.VARCHAR);
			stmt.registerOutParameter(3, Types.VARCHAR);
			stmt.registerOutParameter(4, Types.VARCHAR);
			stmt.executeUpdate();
			rs = (ResultSet) stmt.getObject(1);
			//DEBUG
//			ResultSetMetaData rsmd = rs.getMetaData();
//		    int columnsNumber = rsmd.getColumnCount();
//		    while (rs.next()) {
//		        for (int i = 1; i <= columnsNumber; i++) {
//		            if (i > 1) System.out.print(",  ");
//		            String columnValue = rs.getString(i);
//		            System.out.print(columnValue + " " + rsmd.getColumnName(i));
//		        }
//		        System.out.println("");
//		    }
			consumerList = ResultSetToList(rs);
			rs.close();
			stmt.close();
			con.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return consumerList;
	}
	
	private static List<ConsumerFileReqVO> ResultSetToList(ResultSet rs) {
		List<ConsumerFileReqVO> cfList = new ArrayList<ConsumerFileReqVO>();
		try {	
			while (rs.next()) {
				ConsumerFileReqVO vo;
				int id = rs.getInt("ROW_ID");
				int ftId = rs.getInt("FILE_TYPE_ID");
				int cId = rs.getInt("CONSUMER_ID");
				String fileType = rs.getString("FILE_TYPE");
				String consumer = rs.getString("CONSUMER");
				vo = new ConsumerFileReqVO(id, ftId, fileType, cId, consumer);
				cfList.add(vo);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cfList;
	}

}
