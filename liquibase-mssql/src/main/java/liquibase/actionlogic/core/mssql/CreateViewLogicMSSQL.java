package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.CreateViewAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.CreateViewLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.structure.ObjectName;
import liquibase.structure.core.View;

public class CreateViewLogicMSSQL extends CreateViewLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        ActionResult result = super.execute(action, scope);
        if (action.get(CreateViewAction.Attr.replaceIfExists, false)) {
            Database database = scope.get(Scope.Attr.database, Database.class);
            ObjectName viewName = action.get(CreateViewAction.Attr.viewName, ObjectName.class);

            //from http://stackoverflow.com/questions/163246/sql-server-equivalent-to-oracles-create-or-replace-view
            return new DelegateResult(new ExecuteSqlAction(
                    "IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'["+ viewName.getContainer().getName() +"].["+viewName.getName()+"]')) "
                    + "EXEC sp_executesql N'CREATE VIEW "+database.escapeObjectName(viewName, View.class)+"] AS SELECT ''This is a code stub which will be replaced by an Alter Statement'' as [code_stub]'"),
                    ((DelegateResult) result).getActions().get(0));
        }

        return result;
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);

        if (action.get(CreateViewAction.Attr.replaceIfExists, false)) {
            clauses.replace(Clauses.createStatement, "ALTER VIEW");
        }

        return clauses;
    }
}