/**
 * https://github.com/nakupanda/bootstrap3-dialog
 */

function showInfo(msg) {
    BootstrapDialog.show({
                type: BootstrapDialog.TYPE_INFO,
                title: 'info',
                message: msg,
                buttons: [okButton()]
            });     
}

function showError(msg) {
    BootstrapDialog.show({
                type: BootstrapDialog.TYPE_DANGER,
                title: 'error',
                message: msg,
                buttons: [okButton()]
            });     
}

function okButton() {
    return {
        id: 'btn-ok',   
        icon: 'glyphicon glyphicon-check',       
        label: 'OK',
        cssClass: 'btn-primary', 
        autospin: false,
        action: function(dialogRef){    
            dialogRef.close();
        }
    };
}