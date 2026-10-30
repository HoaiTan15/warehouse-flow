package nosql.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NhatKyThaoTacRepository {
    private static final String COLLECTION_NAME = "nhat_ky_thao_tac_kho";
    private MongoCollection<Document> collection;

    public NhatKyThaoTacRepository() {
        MongoDatabase database = MongoDBConnection.getDatabase();
        if (database != null) {
            this.collection = database.getCollection(COLLECTION_NAME);
            createIndexes();
        }
    }

    private void createIndexes() {
        try {
            collection.createIndex(Indexes.descending("thoiGian"));
            collection.createIndex(Indexes.ascending("loaiThaoTac"));
            collection.createIndex(Indexes.ascending("maDoiTuong"));
            collection.createIndex(Indexes.ascending("tenDangNhap"));
            System.out.println("Indexes created for " + COLLECTION_NAME);
        } catch (Exception e) {
            System.err.println("Failed to create indexes: " + e.getMessage());
        }
    }

    public boolean ghiNhatKy(String loaiThaoTac, String maDoiTuong, String tenDangNhap,
                             String ketQua, String chiTiet) {
        try {
            if (collection == null) return false;

            Document doc = new Document("loaiThaoTac", loaiThaoTac)
                .append("maDoiTuong", maDoiTuong)
                .append("tenDangNhap", tenDangNhap)
                .append("thoiGian", new Date())
                .append("ketQua", ketQua)
                .append("chiTiet", chiTiet);

            collection.insertOne(doc);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to write log: " + e.getMessage());
            return false;
        }
    }

    public List<Document> layNhatKyTheoLoai(String loaiThaoTac, int limit) {
        List<Document> result = new ArrayList<>();
        try {
            if (collection == null) return result;

            collection.find(Filters.eq("loaiThaoTac", loaiThaoTac))
                .sort(Sorts.descending("thoiGian"))
                .limit(limit)
                .into(result);
        } catch (Exception e) {
            System.err.println("Failed to get logs by type: " + e.getMessage());
        }
        return result;
    }

    public List<Document> layNhatKyTheoMaDoiTuong(String maDoiTuong, int limit) {
        List<Document> result = new ArrayList<>();
        try {
            if (collection == null) return result;

            collection.find(Filters.eq("maDoiTuong", maDoiTuong))
                .sort(Sorts.descending("thoiGian"))
                .limit(limit)
                .into(result);
        } catch (Exception e) {
            System.err.println("Failed to get logs by object: " + e.getMessage());
        }
        return result;
    }

    public List<Document> layNhatKyTheoNguoiDung(String tenDangNhap, int limit) {
        List<Document> result = new ArrayList<>();
        try {
            if (collection == null) return result;

            collection.find(Filters.eq("tenDangNhap", tenDangNhap))
                .sort(Sorts.descending("thoiGian"))
                .limit(limit)
                .into(result);
        } catch (Exception e) {
            System.err.println("Failed to get logs by user: " + e.getMessage());
        }
        return result;
    }

    public List<Document> layNhatKyMoiNhat(int limit) {
        List<Document> result = new ArrayList<>();
        try {
            if (collection == null) return result;

            collection.find()
                .sort(Sorts.descending("thoiGian"))
                .limit(limit)
                .into(result);
        } catch (Exception e) {
            System.err.println("Failed to get recent logs: " + e.getMessage());
        }
        return result;
    }
}
