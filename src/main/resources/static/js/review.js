import validation from "./validation.js";
import mask from "./mask.js";

var main = {
    init: async function () {
        let _this = this;

        mask.loadingWithMask();

        const reviewSaveBtn = document.getElementById("review-save");
        const reviewDeleteBtnList = document.querySelectorAll(".review-delete");
        const reviewMoreBtn = document.getElementById("review-more");
        const storeImgBtn = document.getElementById("store-image-btn");
        const mainImg = document.getElementById("main-image");

        if (reviewSaveBtn !== null) {
            reviewSaveBtn.addEventListener("click", () => {
                _this.check()
            });
        }

        if (reviewDeleteBtnList !== null) {
            reviewDeleteBtnList.forEach((btn) => {
                btn.addEventListener("click", () => {
                    _this.delete(btn.id);
                })
            })
        }

        if (reviewMoreBtn !== null) {
            reviewMoreBtn.addEventListener("click", () => {
                _this.more()
            });
        }

        if (storeImgBtn !== null) {
            if (mainImg.getAttribute("src") === null || mainImg.getAttribute("src") === "") {
                storeImgBtn.addEventListener("click", () => {
                    _this.saveMainImage();
                })
            } else {
                storeImgBtn.addEventListener("click", () => {
                    _this.updateMainImage();
                })
            }
        }

        /*
            메뉴판 modal zoom 로직
         */
        const zoomDiv = document.getElementById("modal-content");
        let scale = 1;

        function zoom(event) {
            event.preventDefault();

            scale += event.deltaY * -0.01;

            // Restrict scale
            scale = Math.min(Math.max(.125, scale), 4);

            // Apply scale transform
            zoomDiv.style.transform = `scale(${scale})`;
        }

        if (zoomDiv !== null) {
            zoomDiv.onwheel = zoom;
        }

        mask.closeMask();

        //좋아요 버튼
        let likeBtn =$(".icon.heart");

        likeBtn.click(function(){
            console.log("likeBtn.src=", document.getElementById("like-img").src);
            if (document.getElementById("like-img").src === "https://cdn-icons-png.flaticon.com/512/803/803087.png") {
                likeBtn.addClass("active");
            }
            likeBtn.toggleClass("active");

            if(likeBtn.hasClass("active")){
                _this.likeUpdate(true)
                $(this).find("img").attr({
                    "src": "https://cdn-icons-png.flaticon.com/512/803/803087.png",
                    alt:"찜하기 완료"
                });
            }else{
                _this.likeUpdate(false)
                $(this).find("i").removeClass("fas").addClass("far")
                $(this).find("img").attr({
                    "src": "https://cdn-icons-png.flaticon.com/512/812/812327.png",
                    alt:"찜하기"
                })
            }
        })
    },

    check: function () {
        const content = document.getElementById("content");
        const storeId = document.getElementById("storeId").value;
        const memberId = document.getElementById("memberId").value;
        const reviewForm = document.getElementById("review-add-form");
        const file = document.getElementById("file").files;

        const formData = new FormData(reviewForm);
        let ratingForm = formData.get("rating");

        const contentValid = document.getElementById("review-content-valid");
        const fileValid = document.getElementById("review-file-valid");

        content.classList.remove("valid-custom");

        validation.removeValidation(contentValid);
        validation.removeValidation(fileValid);

        let fileSizeCheck;
        let fileCountCheck = true;
        let targetSize = 1024 * 1024 * 2;

        if (file.length === 0) {
            fileSizeCheck = true;
        } else {
            fileSizeCheck = true;
            for (let i = 0; i < file.length; i++) {
                fileSizeCheck = file[i].size <= targetSize;
                if (!fileSizeCheck) {
                    console.log("용량초과")
                    break;
                }
            }
        }

        if (file.length > 5) {
            fileCountCheck = false
        }

        if (content.value !== "" && ratingForm !== null
            && memberId !== "" && storeId !== ""
            && fileSizeCheck && fileCountCheck) {
            this.save();
        }

        if (content.value === "") {
            content.classList.add("valid-custom");
            validation.addValidation(contentValid, "리뷰 내용을 작성해주세요.");
        }

        if (ratingForm === null) {
            alert("별점을 정해주세요.")
        }

        if (!fileSizeCheck) {
            validation.addValidation(fileValid, "이미지의 크기는 2MB를 넘을 수 없습니다.");
        }

        if (file.length > 5) {
            validation.addValidation(fileValid, "이미지는 5개까지 넣을수 있습니다.");
        }

    },

    save: function () {
        mask.loadingWithMask();

        const reviewForm = document.getElementById("review-add-form");
        const storeId = document.getElementById("storeId").value;

        let formData = new FormData(reviewForm);
        let file = formData.get("file");
        if (file.name === "") {
            formData.delete("file");
        }

        axios({
            headers: {
                "Content-Type": "multipart/form-data",
                "Access-Control-Allow_Origin": "*"
            },
            method: "post",
            url: "/user/review",
            data: formData
        }).then((resp) => {
            alert("리뷰가 등록됐습니다.");
            window.location.href = "/store/" + storeId;
            mask.closeMask();
        }).catch((error) => {
            alert("리뷰를 등록할 수 없습니다.");
            console.log(error)
            mask.closeMask();
        });
    },

    delete: function (btnId) {
        const reviewId = btnId.substring(17);
        const storeId = document.getElementById("storeId").value;

        axios({
            method: "delete",
            url: "/review/delete/" + storeId,
            params: {
                reviewId: reviewId
            }
        }).then((resp) => {
            alert("리뷰 삭제가 완료됐습니다.");
            window.location.reload();
        }).catch((error) => {
            console.log(error);
        })
    },

    more: function () {
        let page = $("#reviewBody tr").length / 5 + 1;  //마지막 리스트 번호를 알아내기 위해서 tr태그의 length를 구함.
        const storeId = document.getElementById("storeId").value;

        let loginMember = null;
        let memberRoleList = document.querySelectorAll(".member-role");
        loginMember = memberRoleList.item(0).value

        axios({
            method: "get",
            url: "/review/more",
            params: {
                page: page,
                storeId: storeId
            }
        }).then((resp) => {
            let data = resp.data

            if (data.last) {
                let reviewMore = document.getElementById("review-more");
                reviewMore.remove();
            }
            let reviewBody = document.getElementById("reviewBody");

            for (let contentElement of data.content) {
                let section = document.createElement('section');
                section.setAttribute('id', 'more_list')
                section.innerHTML += "<div>" +
                    "<small class='text-dark fw-bold ms-1 float-end'>" + contentElement.dateDifference + "</small>" +
                    "<small class='text-dark fw-bold ms-1 float-end'>작성일 : </small>" +
                    "<img src='" + contentElement.memberImgUrl + "' width='70' height='70' alt='프로필 사진' id='profile-img'>" +
                    "<span class='text-dark fw-bold ms-1'>" + contentElement.username + "</span>";
                if (loginMember !== null) {
                    if (loginMember === "ADMIN") {
                        section.innerHTML += "<button id='review-delete-btn" + contentElement.reviewId + "' type='button' class='btn btn-dark mt-4 review-delete float-end'> 삭제 </button>";
                    }
                }
                section.innerHTML += "</div>" +
                    "<br>" +
                    "<div class='wrap-star' >" +
                    "<span class='star-rating2'>" +
                    "<span style='width: " + contentElement.rating * 20 + "%'></span>" +
                    "</span>" +
                    "</div>" +
                    "<br>" +
                    "<p>" + contentElement.content + "</p>" +
                    "<div>";

                for (let uploadImgUrl of contentElement.uploadImgUrl) {
                    if (uploadImgUrl != null && uploadImgUrl !== "https://neighbor-build.s3.ap-northeast-2.amazonaws.com/images/defaultImg.png") {
                        section.innerHTML += '<img src="' + uploadImgUrl + '" width="180" height="180" alt="리뷰 사진">';
                    }
                }
                section.innerHTML += "</div></section>";
                reviewBody.appendChild(section);
            }

            const reviewDeleteBtnList = document.querySelectorAll(".review-delete");

            if (reviewDeleteBtnList !== null) {
                reviewDeleteBtnList.forEach((btn) => {
                    btn.addEventListener("click", () => {
                        this.delete(btn.id);
                    })
                })
            }
        }).catch((e) => {
            console.error(e);
        });
    },

    likeUpdate: function (like) {
        const storeId = document.getElementById("storeId").value;
        const memberId = document.getElementById("memberId").value;

        axios({
            method: "put",
            url: "/user/like",
            params: {
                likeStatus: like,
                memberId: memberId,
                storeId: storeId
            }
        }).then((resp) => {
            // let check = resp.data;
            // if (check) {
            //     alert("가게가 찜 등록이 되었습니다.");
            // } else {
            //     alert("가게가 찜 등록이 취소되었습니다.");
            // }

        }).catch((error) => {
            console.log(error);
        });
    },

    saveMainImage: function () {
        if (!this.validMainImageUpload()) {
            alert("이미지의 크기가 2MB를 넘습니다.");
            return;
        }

        mask.loadingWithMask();

        const storeId = document.getElementById("storeId");
        const form = document.getElementById("main-image-form");
        const formData = new FormData(form);

        axios({
            headers: {
                "Content-Type": "multipart/form-data",
                "Access-Control-Allow_Origin": "*"
            },
            method: "post",
            url: "/seller/store/edit-image/" + storeId.value,
            data: formData
        }).then((resp) => {
            alert("메인 이미지가 등록됐습니다.");
            window.location.reload();
            mask.closeMask();
        }).catch((error) => {
            console.log(error)
            mask.closeMask();
        });
    },

    updateMainImage: function () {
        if (!this.validMainImageUpload()) {
            alert("이미지의 크기가 2MB를 넘습니다.");
            return;
        }

        mask.loadingWithMask();

        const storeId = document.getElementById("storeId");
        const form = document.getElementById("main-image-form");
        const formData = new FormData(form);

        axios({
            headers: {
                "Content-Type": "multipart/form-data",
                "Access-Control-Allow_Origin": "*"
            },
            method: "put",
            url: "/seller/store/edit-image/" + storeId.value,
            data: formData
        }).then((resp) => {
            alert("메인 이미지가 수정됐습니다.");
            mask.closeMask();
            window.location.reload();
        }).catch((error) => {
            console.log(error)
            mask.closeMask();
        });
    },

    validMainImageUpload: function () {
        const file = document.getElementById("file").files;

        let fileSizeCheck;
        let targetSize = 1024 * 1024 * 2;

        if (file.length === 0) {
            fileSizeCheck = true;
        } else {
            fileSizeCheck = file[0].size <= targetSize;
        }

        return fileSizeCheck;
    },
};

main.init();