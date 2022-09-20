var main = {
    init: function () {
        var _this = this;
        _this.getCategories();
        const storeUpdateBtn = document.getElementById('store-update');

        storeUpdateBtn.addEventListener('click', () => {
            _this.updateStore();
        })
    },

    updateStore: function () {
        const storeForm = document.getElementById('store-edit-form');
        const id = document.getElementById('storeId').value;

        const formData = new FormData(storeForm);

        for (let key of formData.keys()) {
            console.log(key);
        }

        for (let v of formData.values()) {
            console.log(v);
        }


        axios({
            method: "put",
            url: "/store/edit/" + id,
            data: formData
        }).then((resp) => {
            alert('매장 정보 수정이 완료됐습니다.');
        }).catch((error) => {
            console.log(error);
        })
    },

    getCategories: function () {
        axios({
            method: "get",
            url: "/categoriesHier",
        }).then((resp) => {
            let rootChildren = resp.data.children;
            let mainChildren = this.getMainCategories(rootChildren);

            let midChildren = this.changeMainCategories(mainChildren);
            this.changeMidCategories(midChildren);
        }).catch((e) => {
            console.error(e);
        })
    },

    getMainCategories: function (children) {
        let mainChildren = [];

        // 대분류는 미리 저장함
        for (const rc of children) {
            let mainOption = document.createElement("option");
            mainOption.text = rc.name;
            mainOption.value = rc.categoryId;
            this.categoryLayerEl.main.appendChild(mainOption);

            mainChildren.push(rc.children)
        }

        return mainChildren;
    },

    changeMainCategories: function (mainChildren) {
        let midChildren = [];

        this.categoryLayerEl.main.addEventListener("change", () => {

            this.resetCategories(this.categoryLayerEl.mid, "중분류 선택");
            this.resetCategories(this.categoryLayerEl.sub, "소분류 선택");

            let mainVal = this.categoryLayerEl.main.options
                [this.categoryLayerEl.main.selectedIndex].value;

            for (const mid of mainChildren) {
                console.log(mainChildren);
                console.log(mid);
                for (let i = 0; i < mid.length; i++) {
                    if (mainVal === String(mid[i].parentId)) {
                        let option = document.createElement("option");
                        option.text = mid[i].name;
                        option.value = mid[i].categoryId;
                        main.categoryLayerEl.mid.appendChild(option)
                    }

                    midChildren.push(mid[i].children);
                }
            }
        });
        return midChildren;
    },

    changeMidCategories: function (midChildren) {
        this.categoryLayerEl.mid.addEventListener("change", () => {

            this.resetCategories(this.categoryLayerEl.sub, "소분류 선택");

            let midVal = this.categoryLayerEl.mid.options
                [this.categoryLayerEl.mid.selectedIndex].value;

            for (const sub of midChildren) {
                for (let i = 0; i < sub.length; i++) {
                    if (midVal === String(sub[i].parentId)) {
                        let option = document.createElement("option");
                        option.text = sub[i].name;
                        option.value = sub[i].categoryId;
                        main.categoryLayerEl.sub.appendChild(option)
                    }
                }
            }
        });
    },

    resetCategories: function (categoryEl, text) {
        while (categoryEl.hasChildNodes()) {
            categoryEl.removeChild(categoryEl.firstChild);
        }

        let option = document.createElement("option");

        option.text = text;
        option.value = "";
        categoryEl.appendChild(option);
    },

    categoryLayerEl: {
        main: document.getElementById("main-cate"),
        mid: document.getElementById("mid-cate"),
        sub: document.getElementById("sub-cate")
    },
};

main.init();