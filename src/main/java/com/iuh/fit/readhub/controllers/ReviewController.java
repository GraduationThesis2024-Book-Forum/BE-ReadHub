package com.iuh.fit.readhub.controllers;

import com.iuh.fit.readhub.dto.ApiResponse;
import com.iuh.fit.readhub.dto.ReviewDTO;
import com.iuh.fit.readhub.dto.request.ReviewRequest;
import com.iuh.fit.readhub.models.Review;
import com.iuh.fit.readhub.repositories.ReviewRepository;
import com.iuh.fit.readhub.repositories.UserRepository;
import com.iuh.fit.readhub.services.BookService;
import com.iuh.fit.readhub.services.ReviewReactService;
import com.iuh.fit.readhub.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewReactService reviewReactService;

    @Autowired
    private BookService bookService;

    @GetMapping("/book/{bookId}")
    public ResponseEntity<ApiResponse<?>> getBookReviews(
            @PathVariable Long bookId,
            @RequestParam(value = "currentUserId", required = false) Long currentUserId  // Thêm name rõ ràng cho parameter
    ) {
        var reviewStats = reviewService.getBookReviews(bookId, currentUserId);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Lấy danh sách review thành công")
                .status(200)
                .data(reviewStats)
                .success(true)
                .build());
    }

    @GetMapping("/user/{userId}/book/{bookId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> getUserReview(
            @PathVariable Long bookId,
            @PathVariable Long userId
    ) {
        List<Review> reviews = reviewService.getUserReview(userId, bookId);
        List<ReviewDTO> reviewsDTO = ReviewDTO.fromReviews(reviews, null);
        if (reviews.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Không tìm thấy review")
                    .status(200)
                    .success(true)
                    .build());
        }
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Lấy review thành công")
                .status(200)
                .data(reviewsDTO)
                .success(true)
                .build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> createReview(@RequestBody ReviewRequest request) {
        Review newReview = new Review();
        newReview.setUser(userRepository.findById(request.getUserId()).get());
        newReview.setBookId(request.getBookId());
        newReview.setRating(request.getRating());
        newReview.setReview(request.getReview());


        Review savedReview = reviewService.createReview(newReview);
        double averageRating = reviewService.getAverageRating(request.getBookId());
        bookService.updateAverageRating(request.getBookId(), averageRating);

        ReviewDTO reviewDTO = ReviewDTO.fromReview(savedReview,null);

        return ResponseEntity.ok(ApiResponse.builder()
                .message("Tạo review thành công")
                .status(200)
                .data(reviewDTO)
                .success(true)
                .build());
    }

    @PutMapping
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> updateReview(
            @RequestBody ReviewRequest request
    ) {
        var reviewOpt = reviewRepository.findById(request.getReviewId());
        if (reviewOpt.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Không tìm thấy review")
                    .status(404)
                    .success(false)
                    .build());
        }

        Review review = reviewOpt.get();
        review.setRating(request.getRating());
        review.setReview(request.getReview());
        reviewRepository.save(review);
        ReviewDTO updatedReview = ReviewDTO.fromReview(review,null);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Cập nhật review thành công")
                .status(200)
                .data(updatedReview)
                .success(true)
                .build());
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Xóa review thành công")
                .status(200)
                .success(true)
                .build());
    }

    @PostMapping("/{reviewId}/like/user/{userId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> likeReview(@PathVariable Long reviewId, @PathVariable Long userId) {
        reviewReactService.likeReview(reviewId, userId);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Like review thành công")
                .status(200)
                .success(true)
                .build());
    }

    @PostMapping("/{reviewId}/report/user/{userId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> reportReview(@PathVariable Long reviewId, @PathVariable Long userId) {
        reviewReactService.reportReview(reviewId, userId);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Report review thành công")
                .status(200)
                .success(true)
                .build());
    }
}