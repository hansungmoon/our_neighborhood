import mask from "./mask.js";

var main = {
    init: async function () {
        let _this = this;

        mask.loadingWithMask();
        await _this.findCoords();
    },

    findCoords: async function () {
        let prevNx = this.getCookie("nx");
        let prevNy = this.getCookie("ny");

        let position = await this.getCoords();

        await this.setCurrentPositionData(position);

        let currentNx = this.getCookie("nx");
        let currentNy = this.getCookie("ny");

        // 이전에 쿠키값과 현재 쿠키값이 다른 경우에만 API 호출 
        // 즉, 현재 위치 정보가 변경됐거나 쿠키가 만료됐을 경우에만 API 재호출
        // => 메인 홈페이지 재방문시 로딩 속도 향상
        if (prevNx !== currentNx && prevNy !== currentNy) {
            this.setWeatherInfoWithAPI();
        } else {
            this.setWeatherInfoWithCookies();
        }

        this.setCateImages();
    },

    setCateImages: function () {
        axios({
            method: "get",
            url: "/get-images"
        }).then((resp) => {
            const restaurantImages = resp.data[0];
            const cafeImages = resp.data[1];
            const barImages = resp.data[2];
            const leisureImages = resp.data[3];

            const restDiv = document.getElementById("cate-img1");
            const cafeDiv = document.getElementById("cate-img2");
            const barDiv = document.getElementById("cate-img3");
            const leisureDiv = document.getElementById("cate-img4");

            if (restaurantImages.length > 0) {
                let restNum = this.randomInt(0, restaurantImages.length);
                restDiv.style.backgroundImage = `url(${restaurantImages[restNum]})`;
            }

            if (cafeImages.length > 0) {
                let cafeNum = this.randomInt(0, cafeImages.length);
                cafeDiv.style.backgroundImage = `url(${cafeImages[cafeNum]})`;
            }

            if (barImages.length > 0) {
                let barNum = this.randomInt(0, barImages.length);
                barDiv.style.backgroundImage = `url(${barImages[barNum]})`;
            }

            if (leisureImages.length > 0) {
                let leisureNum = this.randomInt(0, leisureImages.length);
                leisureDiv.style.backgroundImage = `url(${leisureImages[leisureNum]})`;
            }
        }).catch((error) => {
            console.error(error);
        })
    },

    setWeatherInfoWithAPI: function () {
        axios({
            method: "get",
            url: "/weather",
        }).then((resp) => {
            let skyStatus = resp.data.status;
            let currentTmp = resp.data.tmp;
            let currentPop = resp.data.pop;
            let pm10Value = resp.data.pm10Value;

            this.setCookie("skyStatus", skyStatus, 6);
            this.setCookie("tmp", currentTmp, 6);
            this.setCookie("pop", currentPop, 6);
            this.setCookie("pm10Value", pm10Value, 6);

            this.setWeatherInfoInEl(skyStatus, currentTmp, currentPop, pm10Value);

            mask.closeMask();
        }).catch((error) => {
            alert("현재 날씨 정보를 불러올 수 없습니다.");
            console.error(error);
            mask.closeMask();
        })
    },

    setWeatherInfoWithCookies: function () {
        let skyStatus = this.getCookie("skyStatus");
        let currentTmp =  this.getCookie("tmp");
        let currentPop = this.getCookie("pop");
        let pm10Value = this.getCookie("pm10Value");

        this.setWeatherInfoInEl(skyStatus, currentTmp, currentPop, pm10Value);

        mask.closeMask();
    },

    setWeatherInfoInEl: function(skyStatus, currentTmp, currentPop, pm10Value) {
        const _skyStatus = document.getElementById("sky-status");
        const _tmp = document.getElementById("tmp");
        const _pop = document.getElementById("pop");
        const _pm10Value = document.getElementById("pm-10-value");
        const fontAwesome = document.createElement("i")

        if (skyStatus === "SUNNY") {
            fontAwesome.innerHTML = "<i class=\"fa-solid fa-sun orange fs-1\"></i>"
        } else if (skyStatus === "CLOUDY") {
            fontAwesome.innerHTML = "<i class=\"fa-solid fa-cloud-sun fs-1\"></i>"
        } else if (skyStatus === "VERYCLOUDY") {
            fontAwesome.innerHTML = "<i class=\"fa-solid fa-cloud fs-1\"></i>"
        } else if (skyStatus === "RAINY") {
            fontAwesome.innerHTML = "<i class=\"fa-solid fa-umbrella fs-1\"></i>"
        } else if (skyStatus === "SNOWY") {
            fontAwesome.innerHTML = "<i class=\"fa-regular fa-snowflake fs-1\"></i>"
        }

        if (pm10Value <= 30) {
            _pm10Value.innerText = "좋음 (미세먼지 농도 : " + pm10Value + ")";
        } else if (pm10Value <= 80) {
            _pm10Value.innerText = "보통 (미세먼지 농도 : " + pm10Value + ")";
        } else if (pm10Value <= 150) {
            _pm10Value.innerText = "나쁨 (미세먼지 농도 : " + pm10Value + ")";
        } else {
            _pm10Value.innerText = "매우 나쁨 (미세먼지 농도 : " + pm10Value + ")";
        }

        _skyStatus.appendChild(fontAwesome);
        _tmp.innerText = "현재 기온 : " + currentTmp + "℃";
        _pop.innerText = "강수 확률 : " + currentPop + "%";
    },

    getCoords: function (options) {
        return new Promise((resolve, reject) => {
            navigator.geolocation.getCurrentPosition(resolve, reject, options);
        })
    },

    getSidoName: async function (position) {
        const lat = position.coords.latitude;
        const lon = position.coords.longitude;

        let data = await this.getRegionCode(lat, lon);

        let sidoName = data.result[0].region_1depth_name;

        this.setCookie("sidoName", sidoName, 6);
    },

    getRegionCode: function (lat, lon) {
        // 위경도를 이용하여 행정구역정보 얻기
        var geocoder = new kakao.maps.services.Geocoder();

        var loc = new kakao.maps.LatLng(lat, lon);
        return new Promise((resolve, reject) => {
            geocoder.coord2RegionCode(loc.getLng(), loc.getLat(), function (result, status) {
                if (status === kakao.maps.services.Status.OK) {
                    resolve({result});
                } else {
                    reject(status);
                }
            });
        })
    },

    setCurrentPositionData: async function (position) {
        let lat = position.coords.latitude;
        let lon = position.coords.longitude;

        let coords = this.dfs_xy_conv("toXY", lat, lon);

        let nx = coords["nx"];
        let ny = coords["ny"];

        this.setCookie("nx", nx, 6);
        this.setCookie("ny", ny, 6);
        this.setCookie("lat", lat, 6);
        this.setCookie("lon", lon, 6);

        await this.getSidoName(position);
    },

    error: function () {
        alert('현재 위치 정보를 가져올 수 없습니다.');
    },

    params: {
        RE: 6371.00877,     // 지구 반경(km)
        GRID: 5.0,          // 격자 간격(km)
        SLAT1: 30.0,         // 투영 위도1(degree)
        SLAT2: 60.0,        // 투영 위도2(degree)
        OLON: 126.0,        // 기준점 경도(degree)
        OLAT: 38.0,         // 기준점 위도(degree)
        XO: 43,             // 기준점 X좌표(GRID)
        YO: 136             // 기1준점 Y좌표(GRID)
    },

    /*
        LCC DFS 좌표변환 함수
            code : "toXY"(위경도->좌표, v1:위도, v2:경도), "toLL"(좌표->위경도,v1:x, v2:y)

            ** 참고 : https://gist.github.com/fronteer-kr/14d7f779d52a21ac2f16
     */
    dfs_xy_conv: function (code, v1, v2) {
        var DEGRAD = Math.PI / 180.0;
        var RADDEG = 180.0 / Math.PI;

        var re = this.params.RE / this.params.GRID;
        var slat1 = this.params.SLAT1 * DEGRAD;
        var slat2 = this.params.SLAT2 * DEGRAD;
        var olat = this.params.OLAT * DEGRAD;
        var olon = this.params.OLON * DEGRAD;

        var sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);

        var sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;

        var ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);

        var rs = {};

        if (code === "toXY") {
            rs['lat'] = v1;
            rs['lon'] = v2;

            var ra = Math.tan(Math.PI * 0.25 + (v1) * DEGRAD * 0.5);
            ra = re * sf / Math.pow(ra, sn);

            var theta = v2 * DEGRAD - olon;

            if (theta > Math.PI) theta -= 2.0 * Math.PI;
            if (theta < -Math.PI) theta += 2.0 * Math.PI;

            theta *= sn;

            rs['nx'] = Math.floor(ra * Math.sin(theta) + this.params.XO + 0.5);
            rs['ny'] = Math.floor(ro - ra * Math.cos(theta) + this.params.YO + 0.5);
        } else {
            rs['nx'] = v1;
            rs['ny'] = v2;

            var xn = v1 - this.params.XO;
            var yn = ro - v2 + this.params.YO;

            ra = Math.sqrt(xn * xn + yn * yn);

            if (sn < 0.0) -ra;

            var alat = Math.pow((re * sf / ra), (1.0 / sn));
            alat = 2.0 * Math.atan(alat) - Math.PI * 0.5;

            if (Math.abs(xn) <= 0.0) {
                theta = 0.0;
            } else {
                if (Math.abs(yn) <= 0.0) {
                    theta = Math.PI * 0.5;
                    if (xn < 0.0) -theta;
                } else theta = Math.atan2(xn, yn);
            }

            var alon = theta / sn + olon;

            rs['lat'] = alat * RADDEG;
            rs['lon'] = alon * RADDEG;
        }

        return rs;
    },

    setCookie: function (key, value, exp) {
        let date = new Date();
        date.setDate(date.getDate() + (exp * 1000 * 60 * 60)); // 1000 * 60 * 60 = 1시간
        document.cookie = key + "=" + value + "; path=/; expires=" + date.toUTCString() + ";";
    },

    getCookie: function (name) {
        let cookieValue = null;

        if (document.cookie) {
            let array = document.cookie.split((encodeURI(name) + '='));

            if (array.length >= 2) {
                let arraySub = array[1].split(';');
                cookieValue = encodeURI(arraySub[0]);
            }
        }

        return cookieValue;
    },

    randomInt: function (min, max) {
        return Math.floor(Math.random() * (max)) + min;
    }
};

main.init();