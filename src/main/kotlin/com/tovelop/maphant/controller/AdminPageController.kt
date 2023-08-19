package com.tovelop.maphant.controller

import com.tovelop.maphant.configure.security.UserDataService
import com.tovelop.maphant.configure.security.token.TokenAuthToken
import org.springframework.http.ResponseEntity
import com.tovelop.maphant.dto.*
import com.tovelop.maphant.service.AdminPageService
import com.tovelop.maphant.service.BannerService
import com.tovelop.maphant.type.response.Response
import com.tovelop.maphant.type.response.ResponseUnit
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

@Controller
@RequestMapping("/admin")
class AdminPageController(
    @Autowired val adminPageService: AdminPageService,
    @Autowired val bannerService: BannerService,
    @Autowired val userDataService: UserDataService
) {
    @GetMapping("/")
    fun adminPage(): String {
        return "admin_index_page"
    }
    @GetMapping("/board")
    fun listBoardReport(model: Model, @RequestParam sortType: String?): String {
        val findBoardReport = adminPageService.findBoardReport(sortType ?: "reportedAt", 5)
        model.addAttribute("boardReport", findBoardReport)
        return "admin_board_page"
    }
    @GetMapping("/comment")
    fun listCommentReport(model: Model, @RequestParam sortType: String?): String {
        val findCommentReport = adminPageService.findCommentReport(sortType ?: "reportedAt", 5)
        model.addAttribute("commentReport", findCommentReport)
        return "admin_comment_page"
    }
    @GetMapping("/user")
    fun listUserReport(model: Model, @RequestParam sortType: String?): String {
        val findUserList = adminPageService.findAllUserSanction()
        model.addAttribute("userSanction", findUserList)
        return "admin_user_page"
    }
    @GetMapping("/statistics")
    fun listStatistics(model: Model): String {
        // 가상의 데이터 생성 (예시)
        val today = Date()
        val dateInfo = SimpleDateFormat("yyyy.MM.dd. (E)", Locale.KOREA).format(today)
        val infoBoxContent = "유용한 정보를 제공합니다."

        // 그래프 데이터 생성 (예시)
        val visitorData = listOf(100, 150, 80, 120, 200)
        val postCountData = listOf(50, 70, 40, 60, 90)
        val commentCountData = listOf(200, 300, 150, 180, 250)

        // 모델에 데이터 추가
        model.addAttribute("dateInfo", dateInfo)
        model.addAttribute("infoBoxContent", infoBoxContent)
        model.addAttribute("visitorData", visitorData)
        model.addAttribute("postCountData", postCountData)
        model.addAttribute("commentCountData", commentCountData)

        return "admin_statistics_page"
    }

    @GetMapping("/reportInfo/board")
    fun boardReportInfo(@RequestParam boardId: Int): ResponseEntity<Response<List<BoardReportInfoDTO>>> {
        val findBoardReportInfo = adminPageService.findBoardReportInfo(boardId)
        return ResponseEntity.ok().body(Response.success(findBoardReportInfo))
    }

    @GetMapping("/reportInfo/comment")
    fun commentReportInfo(@RequestParam commentId: Int): ResponseEntity<Response<List<CommentReportInfoDTO>>> {
        val findBoardReportInfo = adminPageService.findCommentReportInfo(commentId)
        return ResponseEntity.ok().body(Response.success(findBoardReportInfo))
    }

    @PostMapping("/sanction/board")
    fun sanctionBoard(@RequestParam boardId: Int): ResponseEntity<ResponseUnit> {
        adminPageService.updateBoardSanction(boardId)
        adminPageService.updateBoardReportStateByBoardId(boardId)
        return ResponseEntity.ok(Response.stateOnly(true))
    }

    @PostMapping("/sanction/comment")
    fun sanctionComment(@RequestParam commentId: Int): ResponseEntity<ResponseUnit> {
        adminPageService.updateCommentSanction(commentId)
        adminPageService.updateCommentReportStateByCommentId(commentId)
        return ResponseEntity.ok(Response.stateOnly(true))
    }

    @PostMapping("/sanction/user")
    fun sanctionUser(@RequestBody userReportDTO: UserReportDTO): ResponseEntity<ResponseUnit> {
        //유저 제재 내역 테이블에 이미 존재하는지 확인
        if (adminPageService.findReportByUserId(userReportDTO.userId)) {
            return ResponseEntity.ok(Response.stateOnly(false))
        }
        //유저를 정지 상태(2)로 변경
        adminPageService.updateUserState(userReportDTO.userId, 2)
        //정지할 유저가 작성한 모든 글, 댓글을 임시 블락 상태(3)으로 변경
        adminPageService.updateBoardBlockByUserId(userReportDTO.userId)
        adminPageService.updateCommentBlockByUserId(userReportDTO.userId)
        //유저 제재 내역 테이블에 삽입
        adminPageService.insertUserReport(userReportDTO)
        //유저 정보 업데이트
        userDataService.updateUserDataByUserId(userReportDTO.userId)
        return ResponseEntity.ok(Response.stateOnly(true))
    }

    @PostMapping("/unSanction/user")
    fun unSanctionUser(@RequestParam userId: Int): ResponseEntity<ResponseUnit> {
        //유저 제재 내역 테이블에서 삭제
        adminPageService.deleteRecentUserReportByUserId(userId)
        //유저를 정상 상태(1)로 변경
        adminPageService.updateUserState(userId, 1)
        //정지할 유저가 작성한 모든 글, 댓글을 임시 블락 상태(3)으로 변경
        adminPageService.updateBoardUnblockByUserId(userId)
        adminPageService.updateCommentUnblockByUserId(userId)
        //유저 정보 업데이트
        userDataService.updateUserDataByUserId(userId)
        return ResponseEntity.ok(Response.stateOnly(true))
    }

    @GetMapping("/reportInfo/user")
    fun userReportInfo(@RequestParam userId: Int): ResponseEntity<Response<List<UserReportDTO>>> {
        val findUserReportInfo = adminPageService.findReportInfoByUserId(userId)
        return ResponseEntity.ok().body(Response.success(findUserReportInfo))
    }

    @GetMapping("/login")
    fun loginPage(): String {
        return "admin_login_page"
    }

    @PostMapping("/banner/insert")
    fun insertBanner(@RequestBody bannerDTO: BannerDTO): ResponseEntity<ResponseUnit> {
        bannerService.insertBanner(bannerDTO)
        return ResponseEntity.ok(Response.stateOnly(true))
    }

    @GetMapping("/banner/find/bannerid")
    fun findBannerByBannerId(@RequestParam bannerId: Int): ResponseEntity<Response<BannerDTO>> {
        val findBannerByBannerId = bannerService.findBannerByBannerId(bannerId)
        return ResponseEntity.ok().body(Response.success(findBannerByBannerId))
    }

    @GetMapping("/banner/find/company")
    fun findBannerByCompany(@RequestParam company: String): ResponseEntity<Response<List<BannerDTO>>> {
        val findBannerByCompany = bannerService.findBannerByCompany(company)
        return ResponseEntity.ok().body(Response.success(findBannerByCompany))
    }

    @PostMapping("/banner/update/title")
    fun updateTitleByBannerId(@RequestParam bannerId: Int, @RequestParam title: String): ResponseEntity<ResponseUnit> {
        bannerService.updateTitleByBannerId(bannerId, title)
        return ResponseEntity.ok(Response.stateOnly(true))
    }

    @PostMapping("/banner/update/imagesurl")
    fun updateImagesUrlByBannerId(
        @RequestParam bannerId: Int,
        @RequestParam imagesUrl: String
    ): ResponseEntity<ResponseUnit> {
        bannerService.updateTitleByBannerId(bannerId, imagesUrl)
        return ResponseEntity.ok(Response.stateOnly(true))
    }

    @PostMapping("/banner/update/url")
    fun updateUrlByBannerId(@RequestParam bannerId: Int, @RequestParam url: String): ResponseEntity<ResponseUnit> {
        bannerService.updateTitleByBannerId(bannerId, url)
        return ResponseEntity.ok(Response.stateOnly(true))
    }

    @GetMapping("/banner/get")
    fun getBannerByBannerId(@RequestParam bannerId: Int): ResponseEntity<Response<GetBannerDTO>> {
        val getBannerByBannerId = bannerService.getBannerByBannerId(bannerId)
        return ResponseEntity.ok().body(Response.success(getBannerByBannerId))
    }
}
