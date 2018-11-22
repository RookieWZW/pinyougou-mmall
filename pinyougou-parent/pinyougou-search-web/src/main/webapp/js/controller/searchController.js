app.controller('searchController',function($scope,searchService){
	
	//搜索
	$scope.search=function(){
		searchService.search($scope.searchMap).success(
			function(response){
				$scope.resultMap=response;				
			}
		);		
	}
	
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':''};
	
	$scope.addSearchItem=function(key,value){
		if(key=='category' || key == 'brand'||key=='price'){
			$scope.searchMap[key]=value;
		}else{
			$scope.searchMap.spec[key]=value;
		}
		
		$scope.search();
	}
	
	
	$scope.removeSearchItem=function(key){
		if(key=="category" ||  key=="brand"||key=='price'){//如果是分类或品牌
			$scope.searchMap[key]="";		
		}else{//否则是规格
			delete $scope.searchMap.spec[key];//移除此属性
		}	
		$scope.search();
	}
	
	
});