(function () {
    //refresh token
    var buttonRefreshToken = {
        action: function(){
            var ids = [], i, view = this.view, grid = view.viewGrid, selectedRecords = grid.getSelectedRecords(), idtab = view.tabId ;
            var callback, headers = [], hasilCallback ='';
            var string_id="",postParams = [];
            var command = "";
            var baseContainer = this;

            baseContainer.setDisabled(true);
            headers.push('iseng_di_isi');
            callback = function(rpcResponse, data, rpcRequest) {
                //isc.say(OB.I18N.getLabel('OBEXAPP_SumResult', [data.total]));
                hasilCallback = data.jawaban;
                if (hasilCallback != '') {
                    isc.say(hasilCallback)
                }
                //isc.say('Token : '+hasilCallback);
                console.log('Hasil CB : '+hasilCallback);
                grid.refreshGrid();
                baseContainer.setDisabled(false);
            }
            OB.RemoteCallManager.call('com.tripad.cootrack.handler.RefreshTokenHandler', {headers: headers}, {}, callback);
        },
        buttonType: 'tmc_refreshtoken',
        prompt: 'Refresh Token dari OpenApi',
        updateState: function(){
            var view = this.view, form = view.viewForm, grid = view.viewGrid, selectedRecords = grid.getSelectedRecords();
            if (view.isShowingForm && form.isNew) {
                this.setDisabled(true);
            } else if (view.isEditingGrid && grid.getEditForm().isNew) {
                this.setDisabled(true);
            } else {
                this.setDisabled(selectedRecords.length !== 0);
            }
        }
    };

    var buttonRefreshListAcc = {
        action: function(){
            var ids = [], i, view = this.view, grid = view.viewGrid, selectedRecords = grid.getSelectedRecords(), idtab = view.tabId ;
            var callback, headers = [], hasilCallback ='';
            var string_id="",postParams = [];
            var command = "";
            var baseContainer = this;

            baseContainer.setDisabled(true);
            headers.push('iseng_di_isi');
            callback = function(rpcResponse, data, rpcRequest) {
                //isc.say(OB.I18N.getLabel('OBEXAPP_SumResult', [data.total]));
                hasilCallback = data.jawaban;
                if (hasilCallback != '') {
                    isc.say(hasilCallback)
                }
                //isc.say('Token : '+hasilCallback);
                console.log('Hasil CB : '+hasilCallback);
                grid.refreshGrid();
                baseContainer.setDisabled(false);
            }
            OB.RemoteCallManager.call('com.tripad.cootrack.handler.RefreshListChildAccount', {headers: headers}, {}, callback);
        },
        buttonType: 'tmc_refreshlistacc',
        prompt: 'Refresh List Acc dari OpenApi',
        updateState: function(){
            var view = this.view, form = view.viewForm, grid = view.viewGrid, selectedRecords = grid.getSelectedRecords();
            if (view.isShowingForm && form.isNew) {
                this.setDisabled(true);
            } else if (view.isEditingGrid && grid.getEditForm().isNew) {
                this.setDisabled(true);
            } else {
                this.setDisabled(false);
            }
        }
    };

        var buttonRefreshListBPFromOA = {
        action: function(){
        	
            var ids = [], i, view = this.view, grid = view.viewGrid, selectedRecords = grid.getSelectedRecords(), idtab = view.tabId ;
            var callback, headers = [], hasilCallback ='';
            var string_id="",postParams = [];
            var command = "";
            var baseContainer = this;

            //postParams.action[0] = 'dari toolbar';
            var params = {action:'kosong'};
            
            
            
            isc.TMC_LoadingPopup.create({
              headers: headers,
              view: view,
              params: params,
              actionHandler: 'com.tripad.cootrack.handler.RefreshListBPFromOA'
            }).show(); /* */
		
        	//test
            /*
				var postParams = [];
               postParams['Command'] = "DEFAULT";
               //postParams['IsPopUpCall'] = "1";
               
               OB.Utilities.openProcessPopup(OB.Application.contextUrl + '/org.openbravo.advpaymentmngt.ad_actionbutton/Reconciliation.html', false, postParams, 500, 320);
            	*/
               
               
        },
        buttonType: 'tmc_refreshlistbpfromoa',
        prompt: 'Refresh List Business Partner dari OpenApi',
        updateState: function(){
            var view = this.view, form = view.viewForm, grid = view.viewGrid, selectedRecords = grid.getSelectedRecords();
            if (view.isShowingForm && form.isNew) {
                this.setDisabled(true);
            } else if (view.isEditingGrid && grid.getEditForm().isNew) {
                this.setDisabled(true);
            } else {
                this.setDisabled(false);
            }
        }
    };

    //register button tmc_refreshlistbpfromoa
    OB.ToolbarRegistry.registerButton(buttonRefreshToken.buttonType, isc.OBToolbarIconButton, buttonRefreshToken, 100, ['2FE2C1A87557457E8CF5711190BCAF43']);
    OB.ToolbarRegistry.registerButton(buttonRefreshListAcc.buttonType, isc.OBToolbarIconButton, buttonRefreshListAcc, 100, ['F0AD6F9195E74AE695FAE4E242BBEF74']);
    //OB.ToolbarRegistry.registerButton(buttonRefreshListBPFromOA.buttonType, isc.OBToolbarIconButton, buttonRefreshListBPFromOA, 100, ['220','4EFDD39C475547118AE7570B06624959']);
}());
