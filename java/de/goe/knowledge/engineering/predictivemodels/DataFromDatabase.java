package de.goe.knowledge.engineering.predictivemodels;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class DataFromDatabase implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private String databaseUrl = "jdbc:monetdb://localhost/demo";
	private String databaseUser = "monetdb";
	private String databasePassword = "monetdb";

	ResultSet resultSet;
	ResultSetMetaData resultSetMetaData;
	Connection connection;

	HashMap<Integer, String> patientMortalityMap = new HashMap<>();

	// Arrow
	// boolean isFirst = true;
	// ArrayList<Integer> id1s = new ArrayList<>();
	// ArrayList<Integer> id2s = new ArrayList<>();
	// ArrayList<Double> distances = new ArrayList<>();

	/**
	 * Connects via JDBC to Database and selects all rows from table
	 * 
	 * @throws SQLException
	 */
	private void connectAndSelectFrom(String table) {
		try {
			connection = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
			PreparedStatement st = connection.prepareStatement("SELECT * FROM " + table);
			resultSet = st.executeQuery();
			resultSetMetaData = resultSet.getMetaData();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Connects via JDBC to Database and performs given SQL statement.
	 * 
	 * @param SQLstatement
	 * @throws SQLException
	 */
	private void connectAndExecute(String SQLstatement) {
		try {
			connection = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
			PreparedStatement st = connection.prepareStatement(SQLstatement);
			resultSet = st.executeQuery();
			resultSetMetaData = resultSet.getMetaData();
		} catch (SQLException e) {
			System.err.println(
					"Something went wrong with the connection. Please check if the server is running. \n " + e);
		}
	}

	public HashMap<Integer, Double> getDistancesFrom(String table, int queryPatient) {
		HashMap<Integer, Double> distanceMap = new HashMap<>();
		connectAndExecute("SELECT * FROM " + table + " WHERE id_1 = " + queryPatient + " OR id_2 = " + queryPatient);

		try {
			int key;
			while (resultSet.next()) {
				if (resultSet.getInt("id_1") == queryPatient) {
					key = resultSet.getInt("id_2");
				} else {
					key = resultSet.getInt("id_1");
				}

				distanceMap.put(key, resultSet.getDouble("distance"));
			}
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return distanceMap;
	}

	/**
	 * Creates an appropriate file for the chosen framework.
	 * 
	 * @param features
	 *            List of features to include from DB
	 * @param filename
	 *            Name of the created file including the fileformat
	 * @param frameworkID
	 *            0 for ELKI, 1 for Mahout, 2 for Weka
	 */
	public void createFrameworkFile(ArrayList<String> features, String filename, int frameworkID) {
		String file = "data/" + filename + ".csv";
		Writer writer = null;
		connectAndSelectFrom("mimiciii.patientsdeaths");
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));

			switch (frameworkID) {
			case 0:
				createElkiFile(features, writer);
				break;
			case 1:
				createMahoutFile(features, writer);
				break;
			case 2:
				createWekaFile(features, writer);
				break;

			default:
				System.out.println("Please select one of the given ids: \n" + "0 for ELKI, 1 for Mahout, 2 for Weka");
			}
			writer.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(file + " successfully created.");
	}

	private void createMahoutFile(ArrayList<String> features, Writer writer) throws SQLException, IOException {
		while (resultSet.next()) {
			int j = 0;
			int itemID = 1;
			while (j != features.size()) {
				int id = resultSet.getInt("id");
				String key = id + "";
				writer.write(key + "," + itemID++ + "," + resultSet.getDouble(features.get(j)) + "\n");
				j++;
			}
			patientMortalityMap.put(resultSet.getInt("id"), "" + resultSet.getDouble("death"));
		}
	}

	private void createElkiFile(ArrayList<String> features, Writer writer) throws SQLException, IOException {
		while (resultSet.next()) {
			String label = "DEAD";
			if (resultSet.getDouble("death") == 0.0) {
				label = "ALIVE";
			}

			int j = 0;
			int id = resultSet.getInt("id");
			writer.write(id + " ");

			while (j != features.size() - 1) {
				writer.write(resultSet.getDouble(features.get(j)) + " ");
				j++;
			}
			writer.write(label + "\n");
		}
	}

	private void createWekaFile(ArrayList<String> features, Writer writer) throws IOException, SQLException {
		// Weka Header
		writer.write("@RELATION mortality \n \n");
		for (String feature : features) {
			writer.write("@ATTRIBUTE " + feature + " NUMERIC \n");
		}
		writer.write("@ATTRIBUTE class {0.0, 100.0} \n \n" + "@DATA \n");

		while (resultSet.next()) {
			double alife = 100;
			int j = 0;
			int id = resultSet.getInt("id");
			writer.write(id + ",");

			while (j != features.size()) {
				writer.write(resultSet.getDouble(features.get(j)) + ",");
				j++;
			}
			if (resultSet.getDouble("death") != 0.0) {
				alife = 0;
			}
			writer.write(alife + "\n");
		}
		writer.close();
		connection.close();
	}

	public void createWekaCSVOnlyAlive(ArrayList<String> features, String filename) {
		String file = "data/" + filename + ".arff";
		Writer writer = null;
		connectAndSelectFrom("mimiciii.patientsdeaths");
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
			writer.write("@RELATION alives \n \n");

			int n = 1;
			while (resultSet.next()) {
				double alife = 100;
				int j = 0;
				int id = resultSet.getInt("id");

				if (resultSet.getDouble("death") == 0.0) {
					writer.write(id + ",");
					while (j != features.size()) {
						writer.write(resultSet.getDouble(features.get(j)) + ",");
						j++;
					}
					writer.write(alife + "\n");
					n++;
				}
			}
			System.out.println("We found " + n + " alive patients");
			writer.close();
			connection.close();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(file + " successfully created.");
	}

	/**
	 * Map which assigns each unique patient id its death
	 * 
	 * @param table
	 */
	public void setPatientMortalityMap(HashMap<Integer, String> patientMortalityMap) {
		this.patientMortalityMap = patientMortalityMap;

		if (patientMortalityMap.isEmpty()) {
			connectAndExecute("SELECT id, death FROM mimiciii.patientsdeaths");

			try {
				while (resultSet.next()) {
					patientMortalityMap.put(resultSet.getInt("id"), resultSet.getString("death"));
				}
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public HashMap<Integer, String> getPatientMortalityMap() {
		try {
			return patientMortalityMap;
		} catch (NullPointerException e) {
			setPatientMortalityMap(patientMortalityMap);
			return patientMortalityMap;
		}
	}
}
// public void createSVD() {
// String file = "data/matrix.csv";
// Writer writer = null;
// connectAndSelectFrom("mimiciii.patientsdeaths");
// try {
// writer = new BufferedWriter(new OutputStreamWriter(new
// FileOutputStream(file), "utf-8"));
//
// while (resultSet.next()) {
// writer.write(resultSet.getDouble("icustay_id") + "," +
// resultSet.getDouble("urine_6h") + ","
// + resultSet.getDouble("urine_12h") + "," + resultSet.getDouble("urine_18h") +
// ","
// + resultSet.getDouble("urine_24h") + "," + resultSet.getDouble("hr_12h_max")
// + ","
// + resultSet.getDouble("hr_18h_max") + "," + resultSet.getDouble("hr_24h_max")
// + ","
// + resultSet.getDouble("hr_6h_max") + "," + resultSet.getDouble("map_12h_max")
// + ","
// + resultSet.getDouble("map_18h_max") + "," +
// resultSet.getDouble("map_24h_max") + ","
// + resultSet.getDouble("map_6h_max") + "," + resultSet.getDouble("rr_12h_max")
// + ","
// + resultSet.getDouble("rr_18h_max") + "," + resultSet.getDouble("rr_24h_max")
// + ","
// + resultSet.getDouble("rr_6h_max") + "," + resultSet.getDouble("sbp_12h_max")
// + ","
// + resultSet.getDouble("sbp_18h_max") + "," +
// resultSet.getDouble("sbp_24h_max") + ","
// + resultSet.getDouble("sbp_6h_max") + "," +
// resultSet.getDouble("spo2_12h_max") + ","
// + resultSet.getDouble("spo2_18h_max") + "," +
// resultSet.getDouble("spo2_24h_max") + ","
// + resultSet.getDouble("spo2_6h_max") + "," +
// resultSet.getDouble("temperature_12h_max") + ","
// + resultSet.getDouble("temperature_18h_max") + "," +
// resultSet.getDouble("temperature_24h_max")
// + "," + resultSet.getDouble("temperature_6h_max") + "," +
// resultSet.getDouble("hr_12h_min")
// + "," + resultSet.getDouble("hr_18h_min") + "," +
// resultSet.getDouble("hr_24h_min") + ","
// + resultSet.getDouble("hr_6h_min") + "," + resultSet.getDouble("map_12h_min")
// + ","
// + resultSet.getDouble("map_18h_min") + "," +
// resultSet.getDouble("map_24h_min") + ","
// + resultSet.getDouble("map_6h_min") + "," + resultSet.getDouble("rr_12h_min")
// + ","
// + resultSet.getDouble("rr_18h_min") + "," + resultSet.getDouble("rr_24h_min")
// + ","
// + resultSet.getDouble("rr_6h_min") + "," + resultSet.getDouble("sbp_12h_min")
// + ","
// + resultSet.getDouble("sbp_18h_min") + "," +
// resultSet.getDouble("sbp_24h_min") + ","
// + resultSet.getDouble("sbp_6h_min") + "," +
// resultSet.getDouble("sspo2_12h_min") + ","
// + resultSet.getDouble("spo2_18h_min") + "," +
// resultSet.getDouble("spo2_24h_min") + ","
// + resultSet.getDouble("spo2_6h_min") + "," +
// resultSet.getDouble("temperature_12h_min") + ","
// + resultSet.getDouble("temperature_18h_min") + "," +
// resultSet.getDouble("temperature_24h_min")
// + "," + resultSet.getDouble("temperature_6h_min") + "," +
// resultSet.getDouble("hematocrit_min")
// + "," + resultSet.getDouble("subject_id") + "," +
// resultSet.getDouble("hadm_id") + ","
// + resultSet.getDouble("hematocrit_max") + "," +
// resultSet.getDouble("wbc_min") + ","
// + resultSet.getDouble("wbc_max") + "," + resultSet.getDouble("glucose_min") +
// ","
// + resultSet.getDouble("glucose_max") + "," +
// resultSet.getDouble("bicarbonate_min") + ","
// + resultSet.getDouble("bicarbonate_max") + "," +
// resultSet.getDouble("potassium_min") + ","
// + resultSet.getDouble("potassium_max") + "," +
// resultSet.getDouble("sodium_min") + ","
// + resultSet.getDouble("sodium_max") + "," + resultSet.getDouble("bun_min") +
// ","
// + resultSet.getDouble("bun_max") + "," +
// resultSet.getDouble("ccreatinine_min") + ","
// + resultSet.getDouble("creatinine_max") + "," + resultSet.getDouble("age") +
// ","
// + resultSet.getDouble("vent") + "," + resultSet.getDouble("gcs") + ","
// + resultSet.getDouble("vasopressor") + "," + resultSet.getDouble("gender") +
// ","
// + resultSet.getDouble("icd9_code") + "," + resultSet.getDouble("id") + ","
// + resultSet.getDouble("norm") + "," + resultSet.getDouble("death") + "\n");
// }
// writer.close();
// connection.close();
// } catch (NumberFormatException e) {
// e.printStackTrace();
// } catch (SQLException e) {
// e.printStackTrace();
// } catch (FileNotFoundException e) {
// e.printStackTrace();
// } catch (IOException e) {
// e.printStackTrace();
// }
// System.out.println(file + " successfully created.");
// }
// }

// public void getArrowDistancesWriter(String table) throws Exception {
// ImmutableList.Builder<Field> childrenBuilder = ImmutableList.builder();
// childrenBuilder.add(new Field("int", FieldType.nullable(new ArrowType.Int(32,
// true)), null)); // id_1
// childrenBuilder.add(new Field("int", FieldType.nullable(new ArrowType.Int(32,
// true)), null)); // id_2
// childrenBuilder.add(new Field("double", FieldType.nullable(new
// ArrowType.FloatingPoint(SINGLE)), null)); // distance
// Schema schema = new Schema(childrenBuilder.build(), null);
//
// connectWith("SELECT * FROM mimiciii.MAHOUTCOSINE WHERE id_1 < 6172");
// int b = 0;
// try {
// System.out.println(rs.getFetchSize());
// while (rs.next()) {
// id1s.add(rs.getInt("id_1"));
// id2s.add(rs.getInt("id_2"));
// distances.add(rs.getDouble("distance"));
// if (b == 100704549) {
// break;
// }
// }
// } catch (OutOfMemoryError e) {
//
// }
// con.close();
//
// VectorSchemaRoot root = VectorSchemaRoot.create(schema, new
// RootAllocator(Integer.MAX_VALUE));
// ArrowFileWriter arrowFileWriter = new ArrowFileWriter(root, new
// DictionaryProvider.MapDictionaryProvider(),
// new FileOutputStream(new File("data/arrow.arrow"), true).getChannel());
//
// int batchSize = 50;
// int entries = 100704549;
// arrowFileWriter.start();
//
// for (int i = 0; i < entries;) {
// int toProcessItems = Math.min(batchSize, entries - i);
// root.setRowCount(100);
//
// for (Field field : root.getSchema().getFields()) {
// FieldVector fieldVector = root.getVector(field.getName());
//
// switch (fieldVector.getMinorType()) {
// case INT:
// writeFieldInt(fieldVector, i, toProcessItems, isFirst);
// if (id1s.size() < id2s.size()) {
// isFirst = false;
// } else {
// isFirst = true;
// }
// break;
// case FLOAT4:
// writeFieldFloat4(fieldVector, i, toProcessItems);
// break;
// default:
// throw new Exception(" Not supported yet type: " +
// fieldVector.getMinorType());
// }
// }
// arrowFileWriter.writeBatch();
// i += toProcessItems;
// }
// arrowFileWriter.end();
// arrowFileWriter.close();
// }
//
// private void writeFieldInt(FieldVector fieldVector, int from, int items,
// boolean first) {
// IntVector intVector = (IntVector) fieldVector;
// intVector.setInitialCapacity(items);
// intVector.allocateNew();
// for (int i = 0; i < items; i++) {
// // System.out.println(id1s.isEmpty() + " - " + id1s.get(0));
// if (first) {
// intVector.setSafe(i, 1, id1s.get(0));
// id1s.remove(0);
// } else {
// intVector.setSafe(i, 1, id2s.get(0));
// id2s.remove(0);
// }
// }
// fieldVector.setValueCount(items);
// }
//
// private void writeFieldFloat4(FieldVector fieldVector, int from, int items) {
// Float4Vector float4Vector = (Float4Vector) fieldVector;
// float4Vector.setInitialCapacity(items);
// float4Vector.allocateNew();
// double a;
//
// for (int i = 0; i < items; i++) {
// a = distances.get(0);
// float4Vector.setSafe(i, 1, (float) a);
// distances.remove(0);
// }
// float4Vector.setValueCount(items);
// }
//
// public void mewMortalityCSV() {
// String file = "data/mortality.csv";
// Writer writer = null;
// connect("mimiciii.patientsdeaths");
// try {
// writer = new BufferedWriter(new OutputStreamWriter(new
// FileOutputStream(file), "utf-8"));
// int dead;
// int i = 1;
//
// while (rs.next()) {
// dead = 1;
//
// if (rs.getDouble("death") != 0.0) {
// dead = 0;
// }
//
// writer.write(rs.getDouble("icustay_id") + "," + rs.getDouble("urine_6h") +
// ","
// + rs.getDouble("urine_12h") + "," + rs.getDouble("urine_18h") + "," +
// rs.getDouble("urine_24h")
// + "," + rs.getDouble("hr_12h_max") + "," + rs.getDouble("hr_18h_max") + ","
// + rs.getDouble("hr_24h_max") + "," + rs.getDouble("hr_6h_max") + ","
// + rs.getDouble("map_12h_max") + "," + rs.getDouble("map_18h_max") + ","
// + rs.getDouble("map_24h_max") + "," + rs.getDouble("map_6h_max") + ","
// + rs.getDouble("rr_12h_max") + "," + rs.getDouble("rr_18h_max") + ","
// + rs.getDouble("rr_24h_max") + "," + rs.getDouble("rr_6h_max") + ","
// + rs.getDouble("sbp_12h_max") + "," + rs.getDouble("sbp_18h_max") + ","
// + rs.getDouble("sbp_24h_max") + "," + rs.getDouble("sbp_6h_max") + ","
// + rs.getDouble("spo2_12h_max") + "," + rs.getDouble("spo2_18h_max") + ","
// + rs.getDouble("spo2_24h_max") + "," + rs.getDouble("spo2_6h_max") + ","
// + rs.getDouble("temperature_12h_max") + "," +
// rs.getDouble("temperature_18h_max") + ","
// + rs.getDouble("temperature_24h_max") + "," +
// rs.getDouble("temperature_6h_max") + ","
// + rs.getDouble("hr_12h_min") + "," + rs.getDouble("hr_18h_min") + ","
// + rs.getDouble("hr_24h_min") + "," + rs.getDouble("hr_6h_min") + ","
// + rs.getDouble("map_12h_min") + "," + rs.getDouble("map_18h_min") + ","
// + rs.getDouble("map_24h_min") + "," + rs.getDouble("map_6h_min") + ","
// + rs.getDouble("rr_12h_min") + "," + rs.getDouble("rr_18h_min") + ","
// + rs.getDouble("rr_24h_min") + "," + rs.getDouble("rr_6h_min") + ","
// + rs.getDouble("sbp_12h_min") + "," + rs.getDouble("sbp_18h_min") + ","
// + rs.getDouble("sbp_24h_min") + "," + rs.getDouble("sbp_6h_min") + ","
// + rs.getDouble("sspo2_12h_min") + "," + rs.getDouble("spo2_18h_min") + ","
// + rs.getDouble("spo2_24h_min") + "," + rs.getDouble("spo2_6h_min") + ","
// + rs.getDouble("temperature_12h_min") + "," +
// rs.getDouble("temperature_18h_min") + ","
// + rs.getDouble("temperature_24h_min") + "," +
// rs.getDouble("temperature_6h_min") + ","
// + rs.getDouble("subject_id") + +rs.getDouble("hadm_id") + "," +
// rs.getDouble("hematocrit_min")
// + "," + rs.getDouble("hematocrit_max") + "," + rs.getDouble("wbc_min") + ","
// + rs.getDouble("wbc_max") + "," + rs.getDouble("glucose_min") + ","
// + rs.getDouble("glucose_max") + "," + rs.getDouble("bicarbonate_min") + ","
// + rs.getDouble("bicarbonate_max") + "," + rs.getDouble("potassium_min") + ","
// + rs.getDouble("potassium_max") + "," + rs.getDouble("sodium_min") + ","
// + rs.getDouble("sodium_max") + "," + rs.getDouble("bun_min") + "," +
// rs.getDouble("bun_max")
// + "," + rs.getDouble("ccreatinine_min") + "," +
// rs.getDouble("creatinine_max") + ","
// + rs.getDouble("age") + "," + rs.getDouble("vent") + "," +
// rs.getDouble("gcs") + ","
// + rs.getDouble("vasopressor") + "," + rs.getDouble("gender") + "," +
// rs.getDouble("icd9_code")
// + "," + rs.getDouble("id") + "," + rs.getDouble("norm") + "," + dead + "\n");
// }
// writer.close();
// con.close();
// } catch (NumberFormatException e) {
// e.printStackTrace();
// } catch (SQLException e) {
// e.printStackTrace();
// } catch (FileNotFoundException e) {
// e.printStackTrace();
// } catch (IOException e) {
// e.printStackTrace();
// }
// System.out.println(file + " successfully created.");
// }
// }
