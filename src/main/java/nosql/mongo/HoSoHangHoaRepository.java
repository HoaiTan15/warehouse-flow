package nosql.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import model.HangHoa;

public class HoSoHangHoaRepository {
    private static final String COLLECTION_NAME = "ho_so_hang_hoa";
    private MongoCollection<Document> collection;

    public HoSoHangHoaRepository() {
        MongoDatabase database = MongoDBConnection.getDatabase();
        if (database != null) {
            this.collection = database.getCollection(COLLECTION_NAME);
            createIndexes();
        }
    }

    private void createIndexes() {
        try {
            collection.createIndex(Indexes.ascending("maHang"), new IndexOptions().unique(true));
            collection.createIndex(Indexes.ascending("tags"));
            collection.createIndex(Indexes.ascending("ngayCapNhat"));
            System.out.println("Indexes created for " + COLLECTION_NAME);
        } catch (Exception e) {
            System.err.println("Failed to create indexes: " + e.getMessage());
        }
    }

    public boolean themHoSo(String maHang, String moTaChiTiet, Map<String, Object> thuocTinhMoRong,
                            List<String> tags, String ghiChu) {
        try {
            if (collection == null) return false;

            Document doc = new Document("maHang", maHang)
                .append("moTaChiTiet", moTaChiTiet)
                .append("thuocTinhMoRong", thuocTinhMoRong != null ? new Document(thuocTinhMoRong) : new Document())
                .append("tags", tags != null ? tags : new ArrayList<>())
                .append("ghiChu", ghiChu)
                .append("ngayCapNhat", new Date());

            collection.insertOne(doc);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to insert ho so: " + e.getMessage());
            return false;
        }
    }

    public Document layHoSoTheoMaHang(String maHang) {
        try {
            if (collection == null) return null;
            return collection.find(Filters.eq("maHang", maHang)).first();
        } catch (Exception e) {
            System.err.println("Failed to get ho so: " + e.getMessage());
            return null;
        }
    }

    public boolean capNhatHoSo(String maHang, String moTaChiTiet, Map<String, Object> thuocTinhMoRong,
                               List<String> tags, String ghiChu) {
        try {
            if (collection == null) return false;

            Document updates = new Document();
            if (moTaChiTiet != null) updates.append("moTaChiTiet", moTaChiTiet);
            if (thuocTinhMoRong != null) updates.append("thuocTinhMoRong", new Document(thuocTinhMoRong));
            if (tags != null) updates.append("tags", tags);
            if (ghiChu != null) updates.append("ghiChu", ghiChu);
            updates.append("ngayCapNhat", new Date());

            collection.updateOne(
                Filters.eq("maHang", maHang),
                new Document("$set", updates)
            );
            return true;
        } catch (Exception e) {
            System.err.println("Failed to update ho so: " + e.getMessage());
            return false;
        }
    }

    public boolean xoaHoSo(String maHang) {
        try {
            if (collection == null) return false;
            collection.deleteOne(Filters.eq("maHang", maHang));
            return true;
        } catch (Exception e) {
            System.err.println("Failed to delete ho so: " + e.getMessage());
            return false;
        }
    }

    public List<Document> timKiemTheoTag(String tag) {
        List<Document> result = new ArrayList<>();
        try {
            if (collection == null) return result;
            collection.find(Filters.in("tags", tag)).into(result);
        } catch (Exception e) {
            System.err.println("Failed to search by tag: " + e.getMessage());
        }
        return result;
    }

    public List<Document> layTatCaHoSo() {
        List<Document> result = new ArrayList<>();
        try {
            if (collection == null) return result;
            collection.find().into(result);
        } catch (Exception e) {
            System.err.println("Failed to get all ho so: " + e.getMessage());
        }
        return result;
    }

    public void dongBoHangHoa(HangHoa hang, boolean chiTaoMoi) {
        if (collection == null || hang == null || hang.getMaHang() == null) return;
        Document moRong = new Document("donViTinh", hang.getDvt())
                .append("maLoai", hang.getMaLoai())
                .append("maViTri", hang.getMaViTri())
                .append("hanMucTonToiThieu", hang.getHanMucTonToiThieu());
        String moTa = hang.getTenHang() + (hang.getQuyCach() == null || hang.getQuyCach().trim().isEmpty()
                ? "" : " - " + hang.getQuyCach());
        Document duLieu = new Document("maHang", hang.getMaHang())
                .append("tenHang", hang.getTenHang())
                .append("moTaChiTiet", moTa)
                .append("thuocTinhMoRong", moRong)
                .append("ngayCapNhat", new Date());
        try {
            if (chiTaoMoi) {
                duLieu.append("tags", new ArrayList<String>());
                collection.updateOne(Filters.eq("maHang", hang.getMaHang()),
                        new Document("$setOnInsert", duLieu), new UpdateOptions().upsert(true));
            } else {
                duLieu.remove("maHang");
                duLieu.remove("thuocTinhMoRong");
                duLieu.append("thuocTinhMoRong.donViTinh", hang.getDvt())
                        .append("thuocTinhMoRong.maLoai", hang.getMaLoai())
                        .append("thuocTinhMoRong.maViTri", hang.getMaViTri())
                        .append("thuocTinhMoRong.hanMucTonToiThieu", hang.getHanMucTonToiThieu());
                collection.updateOne(Filters.eq("maHang", hang.getMaHang()),
                        new Document("$set", duLieu).append("$setOnInsert",
                                new Document("maHang", hang.getMaHang()).append("tags", new ArrayList<String>())),
                        new UpdateOptions().upsert(true));
            }
        } catch (Exception e) {
            System.err.println("Failed to sync product profile " + hang.getMaHang() + ": " + e.getMessage());
        }
    }

    public void xoaHoSoMau() {
        if (collection == null) return;
        try {
            collection.deleteMany(Filters.in("maHang", "HH001", "HH002", "HH003"));
        } catch (Exception e) {
            System.err.println("Failed to remove sample profiles: " + e.getMessage());
        }
    }
}
