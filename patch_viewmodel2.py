import sys

with open('app/src/main/java/com/example/ui/viewmodel/DebtViewModel.kt', 'r') as f:
    content = f.read()

# addSubscriber
content = content.replace("""repository.addSubscriber(name, phone, address, notes, avatarEmoji, status, dueDate, actor, role)
            _toastMessage.emit("تم إضافة المشترك بنجاح.")""", """repository.addSubscriber(name, phone, address, notes, avatarEmoji, status, dueDate, actor, role)
            createSystemNotification("مشترك جديد", "تم إضافة المشترك $name بنجاح.", "NEW_SUBSCRIBER", "MEDIUM")
            _toastMessage.emit("تم إضافة المشترك بنجاح.")""")

# updateSubscriber
content = content.replace("""repository.updateSubscriber(subscriber, actor, role)
            _toastMessage.emit("تم تحديث بيانات المشترك بنجاح.")""", """repository.updateSubscriber(subscriber, actor, role)
            createSystemNotification("تحديث مشترك", "تم تحديث بيانات المشترك ${subscriber.name}.", "ACCOUNT_UPDATE", "LOW", subscriber.id)
            _toastMessage.emit("تم تحديث بيانات المشترك بنجاح.")""")

# deleteSubscriber
content = content.replace("""repository.deleteSubscriber(subscriber, actor, role)
            _toastMessage.emit("تم حذف المشترك بنجاح مع كافة حساباته.")""", """repository.deleteSubscriber(subscriber, actor, role)
            createSystemNotification("حذف مشترك", "تم حذف المشترك ${subscriber.name}.", "ACCOUNT_UPDATE", "HIGH")
            _toastMessage.emit("تم حذف المشترك بنجاح مع كافة حساباته.")""")

# addTransaction
content = content.replace("""val tx = repository.addTransaction(subscriberId, type, amount, notes, actor, role)
            if (tx != null) {
                _toastMessage.emit("تم تسجيل العملية المالية بنجاح.")""", """val tx = repository.addTransaction(subscriberId, type, amount, notes, actor, role)
            if (tx != null) {
                val notifType = if(type == "DEBT") "NEW_DEBT" else "PAYMENT"
                val notifTitle = if(type == "DEBT") "دين جديد" else "دفعة جديدة"
                val priority = if(type == "DEBT") "HIGH" else "MEDIUM"
                createSystemNotification(notifTitle, "تم تسجيل ${if(type == "DEBT") "دين" else "دفعة"} بقيمة $amount", notifType, priority, subscriberId)
                _toastMessage.emit("تم تسجيل العملية المالية بنجاح.")""")

with open('app/src/main/java/com/example/ui/viewmodel/DebtViewModel.kt', 'w') as f:
    f.write(content)
