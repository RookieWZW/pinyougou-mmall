app.controller('searchController', function($scope,$location,searchService) {

	// 搜索
	$scope.search = function() {
		$scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
		searchService.search($scope.searchMap).success(function(response) {
			$scope.resultMap = response;
			$scope.searchMap.pageNo = 1;
			buildPageLabel();
			
		
		});
	}

	$scope.searchMap = {
		'keywords' : '',
		'category' : '',
		'brand' : '',
		'spec' : {},
		'price' : '',
		'pageNo' : 1,
		'pageSize' : 40,
		'sortField' : '',
		'sort' : ''
	};

	$scope.addSearchItem = function(key, value) {
		if (key == 'category' || key == 'brand' || key == 'price') {
			$scope.searchMap[key] = value;
		} else {
			$scope.searchMap.spec[key] = value;
		}

		$scope.search();
	}

	$scope.removeSearchItem = function(key) {
		if (key == "category" || key == "brand" || key == 'price') {// 如果是分类或品牌
			$scope.searchMap[key] = "";
		} else {// 否则是规格
			delete $scope.searchMap.spec[key];// 移除此属性
		}
		$scope.search();
	}

	buildPageLabel = function() {

		$scope.pageLabel = [];// 新增分页栏属性
		var maxPageNo = $scope.resultMap.totalPages;// 得到最后页码
		var firstPage = 1;// 开始页码
		var lastPage = maxPageNo;// 截止页码
		$scope.firstDot = true;
		$scope.lastDot = true;
		if ($scope.resultMap.totalPages > 5) { // 如果总页数大于5页,显示部分页码
			if ($scope.searchMap.pageNo <= 3) {// 如果当前页小于等于3
				lastPage = 5; // 前5页
				$scope.firstDot = false;
			} else if ($scope.searchMap.pageNo >= lastPage - 2) {// 如果当前页大于等于最大页码-2
				firstPage = maxPageNo - 4; // 后5页
				$scope.lastDot = false;// 后边没点
			} else { // 显示当前页为中心的5页
				firstPage = $scope.searchMap.pageNo - 2;
				lastPage = $scope.searchMap.pageNo + 2;
			}
		} else {
			$scope.firstDot = false;// 前面无点
			$scope.lastDot = false;// 后边无点

		}
		// alert( $scope.searchMap.pageNo );
		// 循环产生页码标签
		for (var i = firstPage; i <= lastPage; i++) {
			$scope.pageLabel.push(i);
		}
	}

	$scope.queryByPage = function(pageNo) {
		if (pageNo < 1 || pageNo > $scope.resultMap.totalPages) {
			return;
		}

		$scope.searchMap.pageNo = pageNo;
		$scope.search();

	}

	$scope.isTopPage = function() {
		if ($scope.searchMap.pageNo == 1) {
			return true;
		} else {
			return false;
		}
	}

	$scope.isEndPage = function() {
		if ($scope.searchMap.pageNo == $scope.resultMap.totalPages) {
			return true;
		} else {
			return false;
		}
	}

	$scope.sortSearch = function(sortField, sort) {
		$scope.searchMap.sortField = sortField;
		$scope.searchMap.sort = sort;
		$scope.search();
	}

	$scope.keywordsIsBrand=function(){
		for(var i=0;i<$scope.resultMap.brandList.length;i++){
	if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){//如果包含
				return true;
			}			
		}		
		return false;
	}
	
	
	$scope.loadkeywords=function(){
		
	 
		$scope.searchMap.keywords=  $location.search()['keywords'];
		
		$scope.search();
	}



});