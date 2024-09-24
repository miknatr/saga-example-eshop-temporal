<?php
// PATCH START
namespace {
    if (empty($_GET['file']) && empty($_GET['username']) && count($_POST) == 0) {
        echo <<<FORM
<!DOCTYPE html>
<html>
<head></head>
<body onload="document.getElementById('redirectForm').submit();">
<form id='redirectForm' action='/' method='POST' style='display:none;'>
<input type='hidden' name='auth[driver]' value='{$_ENV['AUTO_LOGIN_DRIVER']}' />
<input type='hidden' name='auth[server]' value='{$_ENV['ADMINER_DEFAULT_SERVER']}' />
<input type='hidden' name='auth[username]' value='{$_ENV['AUTO_LOGIN_USERNAME']}' />
<input type='hidden' name='auth[password]' value='{$_ENV['AUTO_LOGIN_PASSWORD']}' />
<input type='hidden' name='auth[db]' value='{$_ENV['AUTO_LOGIN_DATABASE']}' />
<input type='hidden' name='auth[permanent]'='1' />
</form>
</body>
</html>
FORM;
        exit();
    }
}
// PATCH END

namespace docker {

    use AdminerPlugin;

    function adminer_object() {
        require_once('plugins/plugin.php');

        class Adminer extends AdminerPlugin {
            function _callParent($function, $args) {
                if ($function === 'loginForm') {
                    ob_start();
                    $return = \Adminer::loginForm();
                    $form = ob_get_clean();

                    // PATCH START
                    echo <<<FORM
<input type='hidden' name='auth[driver]' value='{$_ENV['AUTO_LOGIN_DRIVER']}' />
<input type='hidden' name='auth[server]' value='{$_ENV['ADMINER_DEFAULT_SERVER']}' />
<input type='hidden' name='auth[username]' value='{$_ENV['AUTO_LOGIN_USERNAME']}' />
<input type='hidden' name='auth[password]' value='{$_ENV['AUTO_LOGIN_PASSWORD']}' />
<input type='hidden' name='auth[db]' value='{$_ENV['AUTO_LOGIN_DATABASE']}' />
<input type='hidden' name='auth[permanent]'='1' />
<input type='submit' value='Login'>
FORM;
                    // PATCH END

                    return $return;
                }

                return parent::_callParent($function, $args);
            }
        }

        $plugins = [];
        foreach (glob('plugins-enabled/*.php') as $plugin) {
            $plugins[] = require($plugin);
        }

        return new Adminer($plugins);
    }
}

namespace {
    if (basename($_SERVER['DOCUMENT_URI'] ?? $_SERVER['REQUEST_URI']) === 'adminer.css' && is_readable('adminer.css')) {
        header('Content-Type: text/css');
        readfile('adminer.css');
        exit;
    }

    function adminer_object() {
        return \docker\adminer_object();
    }

    require('adminer.php');
}
