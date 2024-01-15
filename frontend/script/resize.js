window.clientDimensions = function(ele){
	if(ele){
		console.log('clientWidth' + ele.clientWidth + 'clientHeight' +  ele.clientHeight + 'offsetWidth' + ele.offsetWidth + 'offsetHeight' +  ele.offsetHeight);
		ele.$server.dimensions(ele.clientWidth,ele.clientHeight, ele.offsetWidth, ele.offsetHeight);
	}
	
}