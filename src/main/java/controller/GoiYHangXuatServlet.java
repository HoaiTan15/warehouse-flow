package controller;

import com.google.gson.Gson;
import model.ProductRecommendationDTO;
import nosql.neo4j.WarehouseGraphService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@WebServlet("/GoiYHangXuatServlet")
public class GoiYHangXuatServlet extends HttpServlet {

    private final WarehouseGraphService graphService;

    public GoiYHangXuatServlet() {
        this.graphService = new WarehouseGraphService();
    }

    public GoiYHangXuatServlet(WarehouseGraphService graphService) {
        this.graphService = graphService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String maHang = request.getParameter("maHang");
        int limit = 4;
        String limitParam = request.getParameter("limit");
        if (limitParam != null) {
            try {
                limit = Math.max(1, Integer.parseInt(limitParam.trim()));
            } catch (Exception ignored) {}
        }

        List<ProductRecommendationDTO> recommendations;
        try {
            recommendations = graphService.recommendRelatedProducts(maHang, limit);
            if (recommendations == null) {
                recommendations = Collections.emptyList();
            }
        } catch (Exception e) {
            System.err.println("GoiYHangXuatServlet error: " + e.getMessage());
            recommendations = Collections.emptyList();
        }

        String json = new Gson().toJson(recommendations);
        response.getWriter().write(json);
    }
}

